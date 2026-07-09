package com.pvsstudio.practice.analyzer.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AnalysisProject {
    private final Path root;

    private AnalysisProject(Path root) {
        this.root = root;
    }

    public static AnalysisProject in(Path root) {
        return new AnalysisProject(root);
    }

    public AnalysisProject file(String relativePath, String content) throws IOException {
        Path file = path(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return this;
    }

    public Path root() {
        return root;
    }

    public Path path(String relativePath) {
        return root.resolve(relativePath);
    }
}
