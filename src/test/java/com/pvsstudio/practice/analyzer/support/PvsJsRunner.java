package com.pvsstudio.practice.analyzer.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PvsJsRunner {
    private final Path executable;

    public PvsJsRunner(Path executable) {
        this.executable = executable;
    }

    public ProcessResult analyze(Path projectDirectory, String... arguments) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(executable.toString());
        command.add("analyze");
        command.addAll(List.of(arguments));
        command.add(projectDirectory.toString());

        Process process = new ProcessBuilder(command)
            .directory(projectDirectory.toFile())
            .start();

        int exitCode = process.waitFor();
        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

        return new ProcessResult(exitCode, stdout, stderr);
    }
}
