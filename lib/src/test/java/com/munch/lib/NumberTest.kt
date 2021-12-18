package com.munch.lib

import com.munch.lib.base.*
import org.junit.Test
import java.nio.ByteBuffer
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Create by munch1182 on 2021/8/23 14:12.
 */
class NumberTest : BaseTest {

    @Test
    fun testBinaryStr() {
        log(
            "MAX_VALUE:\n${binaryStr(Int.MAX_VALUE)},\n"
                    + "${binaryStr(Long.MAX_VALUE)},\n" +
                    "${binaryStr(Short.MAX_VALUE)},\n" +
                    "${binaryStr(Float.MAX_VALUE)}\n" +
                    binaryStr(Double.MAX_VALUE)
        )
        log(
            "MIN_VALUE:\n${binaryStr(Int.MIN_VALUE)},\n"
                    + "${binaryStr(Long.MIN_VALUE)},\n" +
                    "${binaryStr(Short.MIN_VALUE)},\n" +
                    "${binaryStr(Float.MIN_VALUE)}\n" +
                    binaryStr(Double.MIN_VALUE)
        )
    }

    private fun binaryStr(num: Number) = "$num -> toBinaryStr(${num.toBinaryStr()})"

    @Test
    fun testBytes() {
        val bw = ByteBuffer.allocate(28)
        val s: Short = 824
        bw.putInt(824)
        bw.putLong(824L)
        bw.putShort(s)
        bw.putFloat(824f)
        bw.putDouble(824.0)
        bw.putChar('c')
        val array = bw.array()
        log(array.toHexStr())
        log(
            "${824.toBytes().toHexStr()}, ${824L.toBytes().toHexStr()}, " +
                    "${s.toBytes().toHexStr()}, ${824f.toBytes().toHexStr()}, " +
                    "${824.0.toBytes().toHexStr()}, ${'c'.toBytes().toHexStr()}"
        )

        log(
            array.getInt(0),
            array.getLong(4),
            array.getShort(12),
            array.getFloat(14),
            array.getDouble(18),
            array.getChar(26)
        )
        val toBytes = "123???->".toBytes()
        log(toBytes.getString(0, toBytes.size))
    }

    @Test
    fun testFloat(){
        log(3.1415926f.keep(5).toString())
        log(3.1415926f.keep(4).toString())
    }
}