@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper

/**
 * Create by munch1182 on 2021/2/1 9:18.
 */
fun ByteArray.toStr(): String {
    val builder = StringBuilder()
    builder.append("[ ")
    this.forEachIndexed { index, byte ->
        builder.append(byte.toInt().toString())
        if (index != this.size - 1) {
            builder.append(", ")
        }
    }
    builder.append(" ]")
    return builder.toString()
}

fun Char.toByteArray(): ByteArray {
    val e0 = this.toInt()
    val e1 = this.toInt() ushr 8
    return byteArrayOf(e0.toByte(), e1.toByte())
}

fun ByteArray.toChar(start: Int = 0): Char {
    val b0 = this[start].toInt()
    val b1 = this[start + 1].toInt()
    return ((b1 shl 8) or (b0 and 0xff)).toChar()
}

/**
 * int转为4位byte
 */
fun Int.toByteArray(): ByteArray {
    val array = ByteArray(4)
    // 由高位到低位
    array[0] = (this shr 24 and 0xFF).toByte()
    array[1] = (this shr 16 and 0xFF).toByte()
    array[2] = (this shr 8 and 0xFF).toByte()
    array[3] = (this and 0xFF).toByte()
    return array
}

/**
 * 4位byte转为int
 */
fun ByteArray.toInt(start: Int = 0): Int {
    var value = 0
    // 由高位到低位
    for (i in start..(start + 3)) {
        val shift = ((start + 3) - i) * 8
        value += (this[i].toInt() and 0x000000FF) shl shift // 往高位游
    }
    return value
}

/**
 * int转为2位byte数组，不能用于负数，因为符号位会导致计算异常
 */
fun Int.to2ByteArray(): ByteArray {
    if (this < 0) {
        throw  UnsupportedOperationException("int val can not smaller than 0")
    }
    val array = ByteArray(2)
    array[0] = (this and 0xFF).toByte()
    array[1] = (this shr 8 and 0xFF).toByte()
    return array
}

/**
 * 两位byte数组转为int，如果原值是负数则不能得到正确的值
 */
fun ByteArray.toIntBy2Byte(start: Int = 0): Int {
    var value: Int = this[start + 1].toInt() and 255 shl 8
    value += this[start].toInt() and 255
    return value
}

fun Long.toByteArray(): ByteArray {
    val e0 = this and 255L
    val e1 = this ushr 8 and 255L
    val e2 = this ushr 16 and 255L
    val e3 = this ushr 24 and 255L
    return byteArrayOf(
        e0.toByte(), e1.toByte(), e2.toByte(), e3.toByte()
    )
}

fun ByteArray.toLong(start: Int = 0): Long {
    var value = 0L
    // 由高位到低位
    for (i in start..(start + 3)) {
        val shift = ((start + 3) - i) * 8
        value += (this[i].toLong() and 0x000000FF) shl shift // 往高位游
    }
    return value
}