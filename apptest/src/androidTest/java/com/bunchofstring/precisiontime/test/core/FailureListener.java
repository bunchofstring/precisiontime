package com.bunchofstring.precisiontime.test.core;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.logging.Level;

public class FailureListener extends RunListener {

    @Override
    public void testFinished(Description description) throws Exception {
        System.out.println("talldave - runlistener-based testFinished");
        super.testFinished(description);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        System.out.println("talldave - runlistener-based testFailure");
        super.testFailure(failure);
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        System.out.println("talldave - runlistener-based testRunFinished "+result.toString());
        super.testRunFinished(result);
    }
}
