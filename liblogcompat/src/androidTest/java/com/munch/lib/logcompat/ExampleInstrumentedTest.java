package com.munch.lib.logcompat;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();

        assertEquals("com.munch.lib.logcompat.test", appContext.getPackageName());
    }

    @Test
    public void test() {
        log.log();
        log.log(1);
        log.log(1, 2, "ad", 5.0);
        log.log(new String[]{"1", "2"}, new int[]{1, 2, 3}, new char[]{'1', 'b', 'f'});
    }

    private static class log{

        public static void log(Object...objects){
            LogLog.log(new LogLog.Builder().stackClass(log.class),objects);
        }
    }
}
