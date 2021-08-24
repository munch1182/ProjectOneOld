package com.munch.lib.base

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Create by munch1182 on 2021/8/14 16:06.
 */

//<editor-fold desc="toStr">
/**
 * 将数据转为2进制字符串，支持int、long、short、float、double
 *
 * float和double实现依据IEEE754
 *
 * @param format 是否进行格式化，即4位补齐和分位
 * @param fillZero 是否对所有位数进行补0，为false则只在有值的4位上补0
 *
 * @see Char.toBinaryStr
 */
fun Number.toBinaryStr(format: Boolean = true, fillZero: Boolean = false): String {
    val str: String
    val maxWeight: Int
    when (this) {
        is Int -> {
            str = Integer.toBinaryString(this)
            maxWeight = Int.SIZE_BITS / 4
        }
        is Long -> {
            str = java.lang.Long.toBinaryString(this)
            maxWeight = Long.SIZE_BITS / 4
        }
        is Short -> {
            str = Integer.toBinaryString(this.toInt())
            maxWeight = 16 / 4
        }
        is Float -> {
            return java.lang.Float.floatToIntBits(this).toBinaryStr(format, fillZero)
        }
        is Double -> {
            return java.lang.Double.doubleToLongBits(this).toBinaryStr(format, fillZero)
        }
        else -> throw IllegalStateException()
    }
    if (format) {
        return format(str, maxWeight, fillZero)
    }
    return str
}

/**
 * @see Number.toBinaryStr
 */
fun Char.toBinaryStr(format: Boolean = true, fillZero: Boolean = false): String {
    val str = Integer.toBinaryString(this.code)
    if (format) {
        return format(str, 16 / 4, fillZero)
    }
    return str
}

private fun format(str: String, maxWeight: Int, fillZero: Boolean): String {
    val sb = StringBuilder()
    val length = str.length
    val bit = length / 4
    var start = length % 4
    if (fillZero && bit < maxWeight) {
        val count = if (start == 0) maxWeight - bit else maxWeight - 1 - bit
        repeat(count) {
            sb.append("0000").append(" ")
        }
    }
    if (start > 0) {
        repeat(4 - start) { sb.append("0") }
        sb.append(str.substring(0, start))
    }
    repeat(length / 4) {
        if (it != 0 || start != 0) {
            sb.append(" ")
        }
        sb.append(str.substring(start, start + 4))
        start += 4
    }
    return sb.toString()
}

/**
 * byte转为16进制字符串
 */
fun Byte.toHexStr() = String.format("%02X", this)
fun ByteArray.toHexStr() = this.joinToString { it.toHexStr() }
//</editor-fold>

//<editor-fold desc="toBytes">
/**
 * 数字转为byte数组，支持int、long、short、float、double
 *
 * float和double实现依据IEEE754
 *
 * @param bigEndian 是否是大端模式，否则为小端模式
 */
fun Number.toBytes(bigEndian: Boolean = true): ByteArray {
    return when (this) {
        is Long -> ByteArray(8) {
            val index = (if (bigEndian) ((7 - it)) else it) * 8
            (this shr index and 0xFF).toByte()
        }
        is Int -> toBytes(this, 4, bigEndian)
        is Short -> toBytes(this.toInt(), 2, bigEndian)
        is Float -> java.lang.Float.floatToIntBits(this).toBytes(bigEndian)
        is Double -> java.lang.Double.doubleToLongBits(this).toBytes(bigEndian)
        else -> throw IllegalStateException()
    }
}

fun Number.toBytesBig() = toBytes(true)
fun Number.toBytesLittle() = toBytes(false)

private fun toBytes(value: Int, size: Int, bigEndian: Boolean = true): ByteArray {
    return ByteArray(size) {
        val index = (if (bigEndian) ((size - 1 - it)) else it) * 8
        (value shr index and 0xFF).toByte()
    }
}

fun Char.toBytes(bigEndian: Boolean = true) = toBytes(this.code, 2, bigEndian)

fun Char.toBytesLittle() = toBytes(false)
fun Char.toBytesBig() = toBytes(true)

fun String.toBytes(charset: Charset = StandardCharsets.UTF_16BE) = this.toByteArray(charset)
fun String.toBytesBig() = toBytes(StandardCharsets.UTF_16BE)
fun String.toBytesLittle() = toBytes(StandardCharsets.UTF_16LE)
//</editor-fold>

//<editor-fold desc="get">
private fun getIntFromBytes(
    array: ByteArray,
    start: Int = 0,
    length: Int,
    bigEndian: Boolean = true
): Int {
    var res = 0
    repeat(length) {
        val index = (if (bigEndian) ((length - 1 - it)) else it)
        res += (array[start + index].toInt() and 0xFF) shl (it * 8)
    }
    return res
}

fun ByteArray.getInt(start: Int = 0, bigEndian: Boolean = true) =
    getIntFromBytes(this, start, 4, bigEndian)

fun ByteArray.getChar(start: Int = 0, bigEndian: Boolean = true) =
    getIntFromBytes(this, start, 2, bigEndian).toChar()

fun ByteArray.getShort(start: Int = 0, bigEndian: Boolean = true) =
    getIntFromBytes(this, start, 2, bigEndian).toShort()

fun ByteArray.getLong(start: Int = 0, bigEndian: Boolean = true): Long {
    var res = 0L
    val length = 8
    repeat(length) {
        val index = (if (bigEndian) ((length - 1 - it)) else it)
        res += (this[start + index].toLong() and 0xFFL) shl (it * 8)
    }
    return res
}

fun ByteArray.getFloat(start: Int = 0, bigEndian: Boolean = true) =
    java.lang.Float.intBitsToFloat(getInt(start, bigEndian))

fun ByteArray.getDouble(start: Int = 0, bigEndian: Boolean = true) =
    java.lang.Double.longBitsToDouble(getLong(start, bigEndian))

fun ByteArray.getString(
    start: Int,
    length: Int,
    charset: Charset = StandardCharsets.UTF_16BE
) = String(this.filterIndexed { index, _ -> index < start + length }.toByteArray(), charset)

fun ByteArray.getStringBig(start: Int, length: Int) =
    getString(start, length, StandardCharsets.UTF_16BE)

fun ByteArray.getStringLittle(start: Int, length: Int) =
    getString(start, length, StandardCharsets.UTF_16LE)
//</editor-fold>