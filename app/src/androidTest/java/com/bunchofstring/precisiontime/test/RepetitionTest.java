package com.bunchofstring.precisiontime.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

public class RepetitionTest {

    @Test
    public void test_Repetition(){
        JUnitCore junitRunner = new JUnitCore();
        Request request = Request.method(CoreUiTest.class, "test_NonDeterministic");
        for(int i = 0; i < 10; i++) {
            System.out.println("talldave - iteration "+(i+1));
            Result result = junitRunner.run(request);
            Assert.assertTrue(result.wasSuccessful());
        }
    }
}
