@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

inline fun Int.hasFlag(flag: Int): Boolean = this and flag == flag

/**
 * 将该数组转为16进制的字符串
 */
fun Number.toHexStr() = Integer.toHexString(this.toInt() and 0xff).completion(2, '0')

/**
 * 将该数据转为至少[len]位的字符串, 如果不足位数, 则在前面补齐[prefix]
 */
fun Number.completion(len: Int, prefix: Char = '0') = toString().completion(len, prefix)

/**
 * 将该值通过16进制的方式转为正值
 */
fun Byte.int() = this.toInt() and 0xff

/**
 * 将该[ByteArray]转为16进制的字符串
 */
fun ByteArray.toHexStr(separator: CharSequence = ", ") =
    this.joinToString(separator) { it.toHexStr() }

//<editor-fold desc="arr split">
/**
 * 将数组根据[unit]长度分割
 */
fun ByteArray.split(unit: Int): Array<ByteArray> {
    val end = size % unit
    val arrSize = if (end == 0) (size / unit) else (size / unit + 1)
    return Array(arrSize) {
        val l = if (it == arrSize - 1 && end > 0) end else arrSize
        val array = ByteArray(l)
        System.arraycopy(this, it * unit, array, 0, l)
        array
    }
}

fun CharArray.split(unit: Int): Array<CharArray> {
    val end = size % unit
    val arrSize = if (end == 0) (size / unit) else (size / unit + 1)
    return Array(arrSize) {
        val l = if (it == arrSize - 1 && end > 0) end else arrSize
        val array = CharArray(l)
        System.arraycopy(this, it * unit, array, 0, l)
        array
    }
}

fun IntArray.split(unit: Int): Array<IntArray> {
    val end = size % unit
    val arrSize = if (end == 0) (size / unit) else (size / unit + 1)
    return Array(arrSize) {
        val l = if (it == arrSize - 1 && end > 0) end else arrSize
        val array = IntArray(l)
        System.arraycopy(this, it * unit, array, 0, l)
        array
    }
}

fun LongArray.split(unit: Int): Array<LongArray> {
    val end = size % unit
    val arrSize = if (end == 0) (size / unit) else (size / unit + 1)
    return Array(arrSize) {
        val l = if (it == arrSize - 1 && end > 0) end else arrSize
        val array = LongArray(l)
        System.arraycopy(this, it * unit, array, 0, l)
        array
    }
}

fun FloatArray.split(unit: Int): Array<FloatArray> {
    val end = size % unit
    val arrSize = if (end == 0) (size / unit) else (size / unit + 1)
    return Array(arrSize) {
        val l = if (it == arrSize - 1 && end > 0) end else arrSize
        val array = FloatArray(l)
        System.arraycopy(this, it * unit, array, 0, l)
        array
    }
}

fun DoubleArray.split(unit: Int): Array<DoubleArray> {
    val end = size % unit
    val arrSize = if (end == 0) (size / unit) else (size / unit + 1)
    return Array(arrSize) {
        val l = if (it == arrSize - 1 && end > 0) end else arrSize
        val array = DoubleArray(l)
        System.arraycopy(this, it * unit, array, 0, l)
        array
    }
}

inline fun <reified T> Array<T>.split(unit: Int): Array<Array<T>> {
    val end = size % unit
    val arrSize = if (end == 0) (size / unit) else (size / unit + 1)
    return Array(arrSize) {
        val l = if (it == arrSize - 1 && end > 0) end else arrSize
        arrayOf<T>().apply {
            System.arraycopy(this, it * unit, this, 0, l)
        }
    }
}
//</editor-fold>