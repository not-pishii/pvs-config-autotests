package com.pvsstudio.practice.analyzer;

import static org.assertj.core.api.Assertions.assertThat;

import com.pvsstudio.practice.analyzer.support.PvsJsCliScenario;
import com.pvsstudio.practice.analyzer.support.PvsJsCliScenarioRun;
import com.pvsstudio.practice.analyzer.support.PvsJsExecutable;
import com.pvsstudio.practice.analyzer.support.PvsJsRunner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PvsJsCliSpecificationTest {
    private static final String MINIMAL_SOURCE = "const value = 1;\n";

    private final PvsJsRunner runner = new PvsJsRunner(PvsJsExecutable.resolve());

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Writes the report to the input directory when --output is omitted")
    void writesReportIntoInputDirectoryWhenOutputFlagIsOmitted() throws Exception {
        // Spec: NewAnalyzer_r0.docx [212].
        // If --output is omitted, the analyzer must create PVS-Studio.json in the input directory.
        PvsJsCliScenarioRun run = PvsJsCliScenario.builder(
            "NewAnalyzer_r0.docx [212]",
            "If --output is omitted, the analyzer writes PVS-Studio.json into the input directory."
        )
            .arrange(project -> project.file("src/test.js", MINIMAL_SOURCE))
            .build()
            .execute(runner, tempDir);

        Path expectedReport = run.path("PVS-Studio.json");
        assertThat(run.result().exitCode())
            .as(run.failureDetails("successful analysis without an explicit --output flag"))
            .isEqualTo(0);
        assertThat(expectedReport)
            .as(run.failureDetails("the default report path is <input-dir>/PVS-Studio.json"))
            .exists();
        assertThat(Files.size(expectedReport))
            .as(run.failureDetails("the default report file is created with non-empty content"))
            .isGreaterThan(0L);
        assertThat(run.result().stdout())
            .as(run.failureDetails("stdout mentions the saved report path for the default output location"))
            .contains(expectedReport.toString());
        assertThat(run.result().stderr())
            .as(run.failureDetails("stderr stays empty for a successful default-output run"))
            .isBlank();
    }

    @Test
    @DisplayName("Uses the last --output value when the same single-value flag is repeated")
    void usesLastOutputValueWhenSingleValueFlagIsRepeated() throws Exception {
        // Spec: NewAnalyzer_r0.docx [199], [212].
        // Repeating a [Single] flag must not stop the application; the last value becomes effective.
        PvsJsCliScenarioRun run = PvsJsCliScenario.builder(
            "NewAnalyzer_r0.docx [199], [212]",
            """
            Repeating a [Single] flag must not stop the application, and the last --output value
            becomes the effective report path.
            """
        )
            .arrange(project -> project.file("src/test.js", MINIMAL_SOURCE))
            .arguments(project -> List.of(
                "--output=" + project.path("reports/first.json"),
                "--output=" + project.path("reports/second.json")
            ))
            .build()
            .execute(runner, tempDir);

        Path firstOutput = run.path("reports/first.json");
        Path secondOutput = run.path("reports/second.json");

        assertThat(run.result().exitCode())
            .as(run.failureDetails("repeated [Single] --output flags keep the analyzer running"))
            .isEqualTo(0);
        assertThat(firstOutput)
            .as(run.failureDetails("the first --output value is overridden by the last one"))
            .doesNotExist();
        assertThat(secondOutput)
            .as(run.failureDetails("the report is written to the last --output path"))
            .exists();
    }
}
