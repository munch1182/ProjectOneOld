package com.munch.lib.android.extend

import junit.framework.TestCase

/**
 * Create by munch1182 on 2022/3/30 09:59.
 */
class StrKtTest : TestCase() {

    /**
     * @see String.count
     */
    fun testCount() {
        val str = "aabbabaabaabbbaa"

        assertEquals(str.count("aa"), 4)
        assertEquals(str.count("ba"), 4)
        assertEquals(str.count("aab"), 3)
        assertEquals(str.count("aaa"), 0)
    }

    /**
     * @see String.split
     */
    fun testSplit() {
        testSplit1()
        testSplit2()
        testSplit3()
    }

    /**
     * @see String.split
     */
    fun testSplit1() {
        val str = "0000000" +
                "1111111" +
                "2222222" +
                "3333333" +
                "4444444" +
                "5555555"

        var split = str.split(7)

        var array = Array(6) { i -> "$i$i$i$i$i$i$i" }

        assertEquals(split.size, array.size)
        repeat(split.size) { assertEquals(split[it], array[it]) }

        split = str.split(6)
        assert(split.size != array.size)

        split = str.split(str.length)
        array = arrayOf(str)
        assertEquals(split.size, array.size)
        repeat(split.size) { assertEquals(split[it], array[it]) }

        split = str.split(str.length + 1, str.length)
        array = arrayOf()
        assertEquals(split.size, array.size)
        repeat(split.size) { assertEquals(split[it], array[it]) }

    }

    fun testSplit2() {
        val str = "0000000" +
                "1111111" +
                "2222222" +
                "3333333" +
                "4444444" +
                "5555555" +
                "66"

        val split = str.split(7)

        val array = Array(7) { i -> if (i == 6) "$i$i" else "$i$i$i$i$i$i$i" }

        assertEquals(split.size, array.size)
        repeat(split.size) { assertEquals(split[it], array[it]) }
    }

    fun testSplit3() {
        val str = "0000000" +
                "1111111" +
                "2222222" +
                "3333333" +
                "4444444" +
                "5555555" +
                "66"

        val split = str.split(7, 2)

        val array = Array(6) { i ->
            val j = i + 1
            "$i$i$i$i$i$j$j"
        }

        assertEquals(split.size, array.size)
        repeat(split.size) { assertEquals(split[it], array[it]) }
    }
}