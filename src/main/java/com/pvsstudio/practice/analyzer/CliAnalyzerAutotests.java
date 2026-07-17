package com.pvsstudio.practice.analyzer;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import java.io.PrintWriter;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

public final class CliAnalyzerAutotests {
    private CliAnalyzerAutotests() {
    }

    static LauncherDiscoveryRequest newTestRunRequest() {
        return LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage(CliAnalyzerAutotests.class.getPackageName()))
                .build();
    }

    public static void main(String[] args) {
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(newTestRunRequest());

        TestExecutionSummary summary = listener.getSummary();
        PrintWriter output = new PrintWriter(System.out, true);
        summary.printTo(output);
        summary.printFailuresTo(output);

        if (summary.getTestsFailedCount() > 0 || summary.getContainersFailedCount() > 0) {
            System.exit(1);
        }
    }
}
