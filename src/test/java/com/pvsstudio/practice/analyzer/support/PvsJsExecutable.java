package com.pvsstudio.practice.analyzer.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PvsJsExecutable {
    private static final String PROPERTY_NAME = "pvsJsPath";
    private static final String ENV_NAME = "PVS_JS_PATH";
    private static final Path DEFAULT_WINDOWS_PATH =
        Paths.get("C:\\Program Files (x86)\\PVS-Studio\\pvs-js\\pvs-js.exe");

    private PvsJsExecutable() {
    }

    public static Path resolve() {
        String configuredPath = System.getProperty(PROPERTY_NAME);
        if (configuredPath == null || configuredPath.isBlank()) {
            configuredPath = System.getenv(ENV_NAME);
        }

        if (configuredPath != null && !configuredPath.isBlank()) {
            return validateExecutable(Paths.get(configuredPath));
        }

        if (Files.isRegularFile(DEFAULT_WINDOWS_PATH)) {
            return DEFAULT_WINDOWS_PATH.toAbsolutePath().normalize();
        }

        throw new IllegalStateException(
            "Path to pvs-js is not configured. Set -D" + PROPERTY_NAME + "=<path> or " + ENV_NAME
                + ", or install pvs-js into the default Windows location: " + DEFAULT_WINDOWS_PATH + "."
        );
    }

    private static Path validateExecutable(Path configuredPath) {
        Path executable = configuredPath.toAbsolutePath().normalize();
        if (!Files.isRegularFile(executable)) {
            throw new IllegalStateException("Configured pvs-js path does not point to a file: " + executable);
        }

        return executable;
    }
}
