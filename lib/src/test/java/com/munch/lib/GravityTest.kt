package com.munch.lib

import com.munch.lib.helper.toBinaryStr
import com.munch.lib.weight.Gravity
import org.junit.Assert
import org.junit.Test

/**
 * Create by munch1182 on 2021/8/14 15:52.
 */
class GravityTest : BaseTest {

    companion object {
        private const val START = Gravity.START
        private const val END = Gravity.END
        private const val TOP = Gravity.TOP
        private const val BOTTOM = Gravity.BOTTOM

        private const val CENTER_HORIZONTAL = Gravity.CENTER_HORIZONTAL
        private const val CENTER_VERTICAL = Gravity.CENTER_VERTICAL
        private const val CENTER = Gravity.CENTER

        fun hasFlag(flags: Int, @Gravity flag: Int) = Gravity.hasFlag(flags, flag)
    }

    /**
     *
     * [1(1), 2(10), 4(100), 8(1000), 3(11), 12(1100)]
     * [5(101), 9(1001), 6(110), 10(1010)]
     * [7(111), 11(1011), 13(1101), 14(1110)]
     * [15(1111)]
     *
     */
    @Test
    fun test() {
        log(START, END, TOP, BOTTOM, CENTER_HORIZONTAL, CENTER_VERTICAL)
        log(START or TOP, START or BOTTOM, END or TOP, END or BOTTOM)
        log(
            CENTER_HORIZONTAL or TOP,
            CENTER_HORIZONTAL or BOTTOM,
            START or CENTER_VERTICAL,
            END or CENTER_VERTICAL
        )
        log("$CENTER(${CENTER.toBinaryStr()})")
        Assert.assertEquals(hasFlag(CENTER, START), true)
        Assert.assertEquals(hasFlag(CENTER, TOP), true)
        Assert.assertEquals(hasFlag(CENTER, CENTER_VERTICAL), true)
        Assert.assertEquals(hasFlag(START or BOTTOM, CENTER_VERTICAL), false)
    }

    @Test
    fun testOP() {
        val num1 = START
        val num2 = BOTTOM
        val num3 = num1 or num2
        log(num1, num2, num3, num1 and num2, num3.xor(num1), num3.xor(num1).or(num1))
        log(num3.xor(num1).or(END).xor(num2).or(CENTER_VERTICAL))
    }

    private fun log(vararg num: Int) {
        log(*Array(num.size) { "${num[it]}(${num[it].toBinaryStr()})" })
    }
}