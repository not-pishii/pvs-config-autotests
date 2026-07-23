package com.pvsstudio.practice.analyzer.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class PvsJsExecutable {
    private static final String PROPERTY_NAME = "pvsJsPath";
    private static final String ENV_NAME = "PVS_JS_PATH";
    private static final List<Path> DEFAULT_WINDOWS_LOCATIONS = List.of(
        Paths.get("C:\\Program Files (x86)\\PVS-Studio\\pvs-js\\pvs-js.exe"),
        Paths.get("C:\\Program Files\\PVS-Studio\\pvs-js\\pvs-js.exe")
    );

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

        Path discoveredExecutable = discoverDefaultInstallation();
        if (discoveredExecutable != null) {
            return discoveredExecutable;
        }

        throw new IllegalStateException(
            "Path to pvs-js is not configured. Set -D" + PROPERTY_NAME + "=<path> or " + ENV_NAME
                + ", or install pvs-js into the default PVS-Studio location."
        );
    }

    private static Path validateExecutable(Path configuredPath) {
        Path executable = configuredPath.toAbsolutePath().normalize();
        if (!Files.isRegularFile(executable)) {
            throw new IllegalStateException("Configured pvs-js path does not point to a file: " + executable);
        }

        return executable;
    }

    private static Path discoverDefaultInstallation() {
        String programFilesX86 = System.getenv("ProgramFiles(x86)");
        if (programFilesX86 != null && !programFilesX86.isBlank()) {
            Path executable = tryValidate(Paths.get(programFilesX86, "PVS-Studio", "pvs-js", "pvs-js.exe"));
            if (executable != null) {
                return executable;
            }
        }

        String programFiles = System.getenv("ProgramFiles");
        if (programFiles != null && !programFiles.isBlank()) {
            Path executable = tryValidate(Paths.get(programFiles, "PVS-Studio", "pvs-js", "pvs-js.exe"));
            if (executable != null) {
                return executable;
            }
        }

        for (Path candidate : DEFAULT_WINDOWS_LOCATIONS) {
            Path executable = tryValidate(candidate);
            if (executable != null) {
                return executable;
            }
        }

        return null;
    }

    private static Path tryValidate(Path candidate) {
        Path executable = candidate.toAbsolutePath().normalize();
        return Files.isRegularFile(executable) ? executable : null;
    }
}
