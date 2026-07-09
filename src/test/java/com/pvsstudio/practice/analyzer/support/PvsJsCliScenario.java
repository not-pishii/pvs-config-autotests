package com.pvsstudio.practice.analyzer.support;

import java.nio.file.Path;
import java.util.List;

public final class PvsJsCliScenario {
    private final String specReference;
    private final String requirement;
    private final ThrowingConsumer<AnalysisProject> projectSetup;
    private final ThrowingFunction<AnalysisProject, List<String>> argumentsFactory;

    private PvsJsCliScenario(
        String specReference,
        String requirement,
        ThrowingConsumer<AnalysisProject> projectSetup,
        ThrowingFunction<AnalysisProject, List<String>> argumentsFactory
    ) {
        this.specReference = specReference;
        this.requirement = requirement;
        this.projectSetup = projectSetup;
        this.argumentsFactory = argumentsFactory;
    }

    public static Builder builder(String specReference, String requirement) {
        return new Builder(specReference, requirement);
    }

    public PvsJsCliScenarioRun execute(PvsJsRunner runner, Path tempDir) throws Exception {
        AnalysisProject project = AnalysisProject.in(tempDir);
        projectSetup.accept(project);
        List<String> arguments = List.copyOf(argumentsFactory.apply(project));
        ProcessResult result = runner.analyze(project.root(), arguments.toArray(String[]::new));
        return new PvsJsCliScenarioRun(specReference, requirement, project, arguments, result);
    }

    public static final class Builder {
        private final String specReference;
        private final String requirement;
        private ThrowingConsumer<AnalysisProject> projectSetup = project -> {
        };
        private ThrowingFunction<AnalysisProject, List<String>> argumentsFactory = project -> List.of();

        private Builder(String specReference, String requirement) {
            this.specReference = specReference;
            this.requirement = requirement;
        }

        public Builder arrange(ThrowingConsumer<AnalysisProject> projectSetup) {
            this.projectSetup = projectSetup;
            return this;
        }

        public Builder arguments(ThrowingFunction<AnalysisProject, List<String>> argumentsFactory) {
            this.argumentsFactory = argumentsFactory;
            return this;
        }

        public PvsJsCliScenario build() {
            return new PvsJsCliScenario(specReference, requirement, projectSetup, argumentsFactory);
        }
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T value) throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T value) throws Exception;
    }
}
