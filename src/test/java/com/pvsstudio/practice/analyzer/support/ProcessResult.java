package com.pvsstudio.practice.analyzer.support;

public record ProcessResult(
    int exitCode,
    String stdout,
    String stderr
) {
}
