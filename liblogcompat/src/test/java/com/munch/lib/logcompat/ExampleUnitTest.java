package com.munch.lib.logcompat;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test() {
        String[] a = {"1", "2", "3", "4"};
        int[] b = {1,2,3,4};
        Object[] c = {1,2,3,4};

        System.out.println(Arrays.asList(a).size() + "///111");
        System.out.println(Arrays.asList(b));
    }
}