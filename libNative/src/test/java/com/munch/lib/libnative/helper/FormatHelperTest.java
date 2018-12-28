package com.munch.lib.libnative.helper;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Munch on 2018/12/28 15:23.
 */
public class FormatHelperTest extends BaseTest {

    @Test
    public void num2Money() {
        log(FormatHelper.num2Money(new BigDecimal("1378427348234"), null));
        log(FormatHelper.num2Money(new BigDecimal("1378427348234"), Locale.FRENCH));
    }

    @Test
    public void num2Str() {
        log(FormatHelper.num2Str(new BigDecimal("1378427348234"), null));
        log(FormatHelper.num2Str(new BigDecimal("1378427348234"), ",#####,#####.00"));
    }

    @Test
    public void str2Date() {
        log(FormatHelper.str2DateNoException("2017/12/03", "yyyy/MM/dd"));
    }

    @Test
    public void date2Str() {
        log(FormatHelper.date2Str(new Date(System.currentTimeMillis() + 30000), "yyyy年MM月dd日 HH:mm:ss"));
    }

    @Test
    public void time2Date() {
        log(FormatHelper.time2Date(System.currentTimeMillis(), null));
    }

    @Test
    public void str2Time() {
        log(FormatHelper.num2Kb(new BigDecimal("719763"),7));
    }


    @Test
    public void getInstance() {
    }
}