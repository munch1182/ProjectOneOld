package com.munch.lib.android.extend

import com.munch.lib.extend.*
import junit.framework.TestCase
import java.nio.charset.StandardCharsets

/**
 * Create by munch1182 on 2022/4/1 13:42.
 */
class NumberKtTest : TestCase() {

    fun testToBinaryStr() {
        assertEquals(1.toShort().toBinaryStr(), "0001")
        assertEquals(1.toChar().toBinaryStr(), "0001")

        assertEquals(
            Int.MAX_VALUE.toBinaryStr(fillZero = true),
            "0111 1111 1111 1111 1111 1111 1111 1111"
        )
        assertEquals(
            Int.MIN_VALUE.toBinaryStr(fillZero = true),
            "1000 0000 0000 0000 0000 0000 0000 0000"
        )
        assertEquals(
            Long.MAX_VALUE.toBinaryStr(fillZero = true),
            "0111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111"
        )
        assertEquals(
            Long.MIN_VALUE.toBinaryStr(fillZero = true),
            "1000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000"
        )

        assertEquals(Int.MAX_VALUE.toBytes().toHexStr(),"0x7F, 0xFF, 0xFF, 0xFF")
    }

    fun testTestToString() {
        assertEquals(3.55555f.toString(2), "3.56")
        assertEquals(3.55455f.toString(2), "3.55")
    }

    fun testToHexStr() {
        assertEquals(0x01.toByte().toHexStr(), "0x01")
    }

    fun testTestToHexStr() {
        val array = byteArrayOf(0x01.toByte(), 0x02.toByte())
        assertEquals(array.toHexStr(), "0x01, 0x02")
    }

    fun testToHexStrSimple() {
        val array = byteArrayOf(0x01.toByte(), 0x02.toByte(), 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)
        assertEquals(
            array.toHexStrSimple(2, array.size - 2),
            "0x01, 0x02, ..., 0x07, 0x08(8 bytes)"
        )
    }

    fun testToBytes() {
        assertEquals(11.toShort().toBytes().toHexStr(), byteArrayOf(0x00, 0x0B).toHexStr())
        assertEquals(
            11.toShort().toBytes(bigEndian = false).toHexStr(),
            byteArrayOf(0x0B, 0x00).toHexStr()
        )
        assertEquals(
            (-11).toShort().toBytes().toHexStr(),
            byteArrayOf(0xFF.toByte(), 0xF5.toByte()).toHexStr()
        )
        assertEquals(
            (-11).toShort().toBytes(bigEndian = false).toHexStr(),
            byteArrayOf(0xF5.toByte(), 0xFF.toByte()).toHexStr()
        )

        assertEquals(
            1L.toBytes().toHexStr(),
            byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01).toHexStr()
        )
    }


    fun testTestToBytes() {
        assertEquals("test".toBytes().toHexStr(), "0x00, 0x74, 0x00, 0x65, 0x00, 0x73, 0x00, 0x74")

        assertEquals(
            "test".toBytes(charset = StandardCharsets.UTF_8).toHexStr(),
            "0x74, 0x65, 0x73, 0x74"
        )
    }

    fun testGetInt() {
        assertEquals(byteArrayOf(0xFF.toByte(), 0xF5.toByte()).getShort(), -11)
        assertEquals(byteArrayOf(0x00.toByte(), 0x0B.toByte()).getShort(), 11)
        assertEquals(byteArrayOf(0x0B.toByte(), 0x00.toByte()).getShort(bigEndian = false), 11)
    }

    fun testGetChar() {}

    fun testGetShort() {}

    fun testGetLong() {}

    fun testGetFloat() {}

    fun testGetDouble() {}

    fun testGetString() {
        assertEquals(
            byteArrayOf(0x00, 0x74, 0x00, 0x65, 0x00, 0x73, 0x00, 0x74).getString(0, 8),
            "test"
        )
        assertEquals(
            byteArrayOf(0x74, 0x65, 0x73, 0x74).getString(0, 4, StandardCharsets.UTF_8),
            "test"
        )
    }

    fun testGetStringBig() {}

    fun testGetStringLittle() {}

    fun testTestSplit() {
        assertEquals(
            byteArrayOf(1, 2, 3, 4, 5, 6, 7)
                .split(3)
                .joinToString { it.toHexStr() },
            arrayOf(
                byteArrayOf(1, 2, 3),
                byteArrayOf(4, 5, 6),
                byteArrayOf(7)
            ).joinToString { it.toHexStr() }
        )
    }

    fun testTestSplit1() {}

    fun testTestSplit2() {}

    fun testTestSplit3() {}

    fun testTestSplit4() {}

    fun testTestSub() {
        assertEquals(
            byteArrayOf(1, 2, 3, 4, 5, 6, 7).sub(0, 3).toHexStr(),
            byteArrayOf(1, 2, 3).toHexStr()
        )
    }

    fun testTestSub1() {}

    fun testTestSub2() {}

    fun testTestSub3() {}

    fun testTestSub4() {}
}