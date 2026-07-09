package com.pvsstudio.practice.analyzer.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PvsJsExecutable {
    private static final String PROPERTY_NAME = "pvsJsPath";
    private static final String ENV_NAME = "PVS_JS_PATH";

    private PvsJsExecutable() {
    }

    public static Path resolve() {
        String configuredPath = System.getProperty(PROPERTY_NAME);
        if (configuredPath == null || configuredPath.isBlank()) {
            configuredPath = System.getenv(ENV_NAME);
        }

        if (configuredPath == null || configuredPath.isBlank()) {
            throw new IllegalStateException(
                "Path to pvs-js is not configured. Set -D" + PROPERTY_NAME + "=<path> or " + ENV_NAME + "."
            );
        }

        Path executable = Paths.get(configuredPath).toAbsolutePath().normalize();
        if (!Files.isRegularFile(executable)) {
            throw new IllegalStateException("Configured pvs-js path does not point to a file: " + executable);
        }

        return executable;
    }
}
