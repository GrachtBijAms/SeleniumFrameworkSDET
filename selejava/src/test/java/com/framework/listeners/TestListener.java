package com.framework.listeners;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("STARTED: " + getDisplayName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("PASSED: " + getDisplayName(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("FAILED: " + getDisplayName(result));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("SKIPPED: " + getDisplayName(result));
    }

    private String getDisplayName(ITestResult result) {
        if (result.getTestName() != null && !result.getTestName().isBlank()) {
            return result.getTestName();
        }
        return result.getMethod().getMethodName();
    }
}
