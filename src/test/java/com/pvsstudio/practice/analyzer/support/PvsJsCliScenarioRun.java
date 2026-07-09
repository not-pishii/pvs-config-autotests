package com.pvsstudio.practice.analyzer.support;

import java.nio.file.Path;
import java.util.List;

public record PvsJsCliScenarioRun(
    String specReference,
    String requirement,
    AnalysisProject project,
    List<String> arguments,
    ProcessResult result
) {
    public Path root() {
        return project.root();
    }

    public Path path(String relativePath) {
        return project.path(relativePath);
    }

    public String failureDetails(String expectation) {
        return """
            %s
            Spec requirement: %s
            Expected: %s
            Input directory: %s
            CLI arguments: %s
            stdout:
            %s
            stderr:
            %s
            """.formatted(
            specReference,
            requirement,
            expectation,
            root(),
            arguments,
            result.stdout(),
            result.stderr()
        );
    }
}
