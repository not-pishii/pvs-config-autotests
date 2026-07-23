package com.pvsstudio.practice.analyzer;

import static org.assertj.core.api.Assertions.assertThat;

import com.pvsstudio.practice.analyzer.support.PvsJsCliScenario;
import com.pvsstudio.practice.analyzer.support.PvsJsCliScenarioRun;
import com.pvsstudio.practice.analyzer.support.PvsJsExecutable;
import com.pvsstudio.practice.analyzer.support.PvsJsRunner;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PvsJsCliSpecificationTest {
    private static final String MINIMAL_SOURCE = "const value = 1;\n";
    private static final String V7003_SOURCE = "const x = 1; if (x > 0) { } else if (x > 0) { }\n";

    private final PvsJsRunner runner = new PvsJsRunner(PvsJsExecutable.resolve());

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Stops on an invalid root pvs-settings.toml")
    void stopsOnInvalidRootToml() throws Exception {
        PvsJsCliScenarioRun run = PvsJsCliScenario.builder(
            "TOML parsing and validation",
            "An invalid root pvs-settings.toml must stop the run with a configuration error."
        )
            .arrange(project -> {
                project.file("src/test.js", MINIMAL_SOURCE);
                project.file("pvs-settings.toml", "!!!");
            })
            .build()
            .execute(runner, tempDir);

        assertThat(run.result().exitCode())
            .as(run.failureDetails("invalid root pvs-settings.toml stops the run"))
            .isEqualTo(2);
        assertThat(run.result().stderr())
            .as(run.failureDetails("stderr points at the invalid root pvs-settings.toml"))
            .contains(run.path("pvs-settings.toml").toString())
            .contains("Unknown token");
        assertThat(run.path("PVS-Studio.json"))
            .as(run.failureDetails("no report is produced when root TOML cannot be parsed"))
            .doesNotExist();
    }

    @Test
    @DisplayName("Stops on a duplicate key in pvs-settings.toml")
    void stopsOnDuplicateRootKey() throws Exception {
        PvsJsCliScenarioRun run = PvsJsCliScenario.builder(
            "TOML parsing and validation",
            "A duplicate key in pvs-settings.toml must be rejected as an invalid configuration file."
        )
            .arrange(project -> {
                project.file("src/test.js", MINIMAL_SOURCE);
                project.file("pvs-settings.toml", "threads = 1\nthreads = 2\n");
            })
            .build()
            .execute(runner, tempDir);

        assertThat(run.result().exitCode())
            .as(run.failureDetails("duplicate root keys stop the run"))
            .isEqualTo(2);
        assertThat(run.result().stderr())
            .as(run.failureDetails("stderr reports the duplicate key"))
            .contains(run.path("pvs-settings.toml").toString())
            .contains("Duplicate key");
        assertThat(run.path("PVS-Studio.json"))
            .as(run.failureDetails("no report is produced when duplicate keys are present"))
            .doesNotExist();
    }

    @Test
    @DisplayName("Ignores an unknown root key")
    void ignoresUnknownRootKey() throws Exception {
        PvsJsCliScenarioRun run = PvsJsCliScenario.builder(
            "Configuration validation",
            "An unknown root key is ignored by the current CLI behavior."
        )
            .arrange(project -> {
                project.file("src/test.js", MINIMAL_SOURCE);
                project.file("pvs-settings.toml", "unknown = 1\n");
            })
            .build()
            .execute(runner, tempDir);

        assertThat(run.result().exitCode())
            .as(run.failureDetails("an unknown root key does not stop the run"))
            .isZero();
        assertThat(run.result().stderr())
            .as(run.failureDetails("the ignored unknown key does not produce a diagnostic"))
            .isBlank();
        assertThat(readReport(run))
            .as(run.failureDetails("analysis still writes the default report"))
            .contains("\"warnings\":[]");
    }

    @Test
    @DisplayName("Ignores .PVS-Studio/additional.rules.toml during default discovery")
    void ignoresAdditionalRulesTomlInDotPvsStudioDirectory() throws Exception {
        PvsJsCliScenarioRun run = PvsJsCliScenario.builder(
            "Configuration level discovery",
            ".PVS-Studio/additional.rules.toml is ignored during the default analyze <dir> flow."
        )
            .arrange(project -> {
                project.file("src/test.js", MINIMAL_SOURCE);
                project.file(".PVS-Studio/additional.rules.toml", "!!!");
            })
            .build()
            .execute(runner, tempDir);

        assertThat(run.result().exitCode())
            .as(run.failureDetails("a malformed additional.rules.toml does not interfere with analysis"))
            .isZero();
        assertThat(run.result().stderr())
            .as(run.failureDetails("stderr stays empty because the malformed file is never parsed"))
            .isBlank();
        assertThat(run.path("PVS-Studio.json"))
            .as(run.failureDetails("analysis still writes the default report"))
            .exists();
    }

    @Test
    @DisplayName("Does not parse nested pvs-settings.toml during root analysis")
    void doesNotParseNestedPvsSettingsTomlDuringRootAnalysis() throws Exception {
        PvsJsCliScenarioRun run = PvsJsCliScenario.builder(
            "Configuration level discovery",
            "During analyze <root> the current CLI behavior does not parse nested pvs-settings.toml files automatically."
        )
            .arrange(project -> {
                project.file("src/test.js", MINIMAL_SOURCE);
                project.file("src/nested/pvs-settings.toml", "!!!");
            })
            .build()
            .execute(runner, tempDir);

        assertThat(run.result().exitCode())
            .as(run.failureDetails("a malformed nested pvs-settings.toml does not stop root analysis"))
            .isZero();
        assertThat(run.result().stderr())
            .as(run.failureDetails("stderr does not mention the nested pvs-settings.toml"))
            .doesNotContain("src\\nested\\pvs-settings.toml")
            .doesNotContain("Unknown token");
        assertThat(run.path("PVS-Studio.json"))
            .as(run.failureDetails("analysis still writes the default report"))
            .exists();
    }

    @Test
    @DisplayName("CLI --rules override suppresses V7003")
    void cliRulesOverrideSuppressesWarning() throws Exception {
        PvsJsCliScenarioRun baseline = PvsJsCliScenario.builder(
            "CLI override priority",
            "Without overrides the fixture emits diagnostic V7003."
        )
            .arrange(project -> project.file("src/test.js", V7003_SOURCE))
            .build()
            .execute(runner, tempDir.resolve("baseline"));

        assertThat(baseline.result().exitCode())
            .as(baseline.failureDetails("baseline analysis succeeds"))
            .isZero();
        assertThat(readReport(baseline))
            .as(baseline.failureDetails("baseline report contains V7003"))
            .contains("\"code\":\"V7003\"");

        PvsJsCliScenarioRun overridden = PvsJsCliScenario.builder(
            "CLI override priority",
            "The --rules CLI override suppresses diagnostic V7003."
        )
            .arrange(project -> project.file("src/test.js", V7003_SOURCE))
            .arguments(project -> java.util.List.of("--rules=V7003=off"))
            .build()
            .execute(runner, tempDir.resolve("overridden"));

        assertThat(overridden.result().exitCode())
            .as(overridden.failureDetails("analysis with CLI override succeeds"))
            .isZero();
        assertThat(readReport(overridden))
            .as(overridden.failureDetails("V7003 is suppressed by the CLI override"))
            .doesNotContain("\"code\":\"V7003\"")
            .contains("\"warnings\":[]");
    }

    private static String readReport(PvsJsCliScenarioRun run) throws Exception {
        return Files.readString(run.path("PVS-Studio.json"));
    }
}
