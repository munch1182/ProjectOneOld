package com.munch.lib.libnative.helper;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.munch.lib.libnative.helper.FormatHelper.BYTE;
import static com.munch.lib.libnative.helper.FormatHelper.GB;
import static com.munch.lib.libnative.helper.FormatHelper.KB;
import static com.munch.lib.libnative.helper.FormatHelper.LEVEL_AUTO;
import static com.munch.lib.libnative.helper.FormatHelper.MB;
import static com.munch.lib.libnative.helper.FormatHelper.TB;

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
        log(FormatHelper.num2KbAll(new BigDecimal("107479040445")).toLowerCase());
        log(FormatHelper.num2Kb(new BigDecimal("107479040445"),3).toLowerCase());
    }


    @Test
    public void getInstance() {
    }
}