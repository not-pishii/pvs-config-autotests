package com.pvsstudio.practice.analyzer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherFactory;

class ProjectSmokeTest {
    @Test
    void mainClasspathTestsAreDiscoverable() {
        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(CliAnalyzerAutotests.newTestRunRequest());

        assertThat(testPlan.containsTests()).isTrue();
    }
}
