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

    private final PvsJsRunner runner = new PvsJsRunner(PvsJsExecutable.resolve());

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Reads pvs-settings.toml from the project root")
    void readsRootPvsSettingsToml() throws Exception {
        // CLI help states that TOML configuration files participate in command processing.
        // An invalid root pvs-settings.toml must therefore stop the run with an argument/config error.
        PvsJsCliScenarioRun run = PvsJsCliScenario.builder(
            "pvs-js analyze --help",
            "An invalid root pvs-settings.toml is treated as a submitted TOML configuration file."
        )
            .arrange(project -> {
                project.file("src/test.js", MINIMAL_SOURCE);
                project.file("pvs-settings.toml", "!!!");
            })
            .build()
            .execute(runner, tempDir);

        assertThat(run.result().exitCode())
            .as(run.failureDetails("invalid root pvs-settings.toml stops the run with a TOML configuration error"))
            .isEqualTo(2);
        assertThat(run.result().stderr())
            .as(run.failureDetails("stderr points at the invalid root pvs-settings.toml file"))
            .contains(run.path("pvs-settings.toml").toString())
            .contains("Unknown token");
        assertThat(run.path("PVS-Studio.json"))
            .as(run.failureDetails("no report is produced when root pvs-settings.toml cannot be parsed"))
            .doesNotExist();
    }

    @Test
    @DisplayName("Ignores .PVS-Studio/additional.rules.toml during default discovery")
    void ignoresAdditionalRulesTomlInDotPvsStudioDirectory() throws Exception {
        // Observed CLI behavior: this file is not consumed automatically during analyze <dir>.
        PvsJsCliScenarioRun run = PvsJsCliScenario.builder(
            "Observed pvs-js analyze behavior",
            ".PVS-Studio/additional.rules.toml is ignored during default configuration discovery."
        )
            .arrange(project -> {
                project.file("src/test.js", MINIMAL_SOURCE);
                project.file(".PVS-Studio/additional.rules.toml", "!!!");
            })
            .build()
            .execute(runner, tempDir);

        assertThat(run.result().exitCode())
            .as(run.failureDetails("an ignored .PVS-Studio/additional.rules.toml does not interfere with analysis"))
            .isEqualTo(0);
        Path expectedReport = run.path("PVS-Studio.json");
        assertThat(expectedReport)
            .as(run.failureDetails("analysis still writes the default report when the ignored TOML file is present"))
            .exists();
        assertThat(Files.size(expectedReport))
            .as(run.failureDetails("the report produced while ignoring .PVS-Studio/additional.rules.toml is non-empty"))
            .isGreaterThan(0L);
        assertThat(run.result().stderr())
            .as(run.failureDetails("stderr stays empty because the malformed ignored file is never parsed"))
            .isBlank();
    }
}
