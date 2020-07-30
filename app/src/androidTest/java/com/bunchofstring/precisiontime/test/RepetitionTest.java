package com.bunchofstring.precisiontime.test;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

public class RepetitionTest {

    @SuppressWarnings("FieldCanBeLocal")
    private final int ITERATIONS = 3;

    private JUnitCore core = new JUnitCore();
    private Throwable throwableSummary = new Throwable("Failed reflective test execution");

    @Test
    public void test_Repetition() throws Throwable {

        Request request = Request.method(CoreUiTest.class, "test_GivenTimeDisplayed_WhenChangeServerUrl_ThenInitiateReSync");
        boolean hasFailures = false;
        for(int i = 0; i < ITERATIONS; i++) {
            System.out.println("talldave - iteration "+(i+1)+" runner="+request);
            if(runTest(request)){
                System.out.println("talldave - iteration2 "+(i+1)+" runner="+request);
                hasFailures = true;
            }
        }
        if(hasFailures){
            throw throwableSummary;
        }
    }

    private boolean runTest(final Request request){
        Result result = core.run(request);
        result.getFailures().forEach(failure -> {
            throwableSummary.addSuppressed(failure.getException());
        });
        return result.wasSuccessful();
    }
}
