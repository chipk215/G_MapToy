package com.keyes_west.mapsgetstarted;


import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class BearingIndexUnitTest {

    private float theta;
    private int expectedResult;

    public BearingIndexUnitTest(float theta, int expectedResult){
        this.theta = theta;
        this.expectedResult = expectedResult;
    }


    @Parameterized.Parameters
    public static Collection testCases(){

        // define test angles (theta) and expected index
        return Arrays.asList(new Object[][]{
                {0.0f, 0},
                {5.0f, 0},
                {11.25f, 0},
                {11.26f, 1},
                {15.0f,1},
                {33.75f, 1},
                {123.75f, 5},
                {-10.0f, 0},
                {-12.24f,15},
                {-350.0f, 0},
                {348.75f,15},
                {236.26f,11}
        });
    }


    @Test
    public void checkBearingIndex(){
        System.out.println("Test theta is: " + theta);
        Assert.assertEquals(expectedResult, MapsActivity.computeBearingIndex(theta));

    }
}
