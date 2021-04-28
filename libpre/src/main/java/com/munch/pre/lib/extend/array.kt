package com.munch.pre.lib.extend

/**
 * Create by munch1182 on 2021/4/28 11:32.
 */
inline fun <reified T> Array<T>.split(length: Int): Array<Array<T>> {
    val end = size % length
    val arraySize = if (end == 0) size / length else (size / length) + 1
    return Array(arraySize) {
        val l = if (it == arraySize - 1 && end > 0) end else length
        arrayOf<T>().apply {
            System.arraycopy(this, it * length, this, 0, l)
        }
    }
}

inline fun <reified T> Array<T?>.splitNullable(length: Int): Array<Array<T?>> {
    val end = size % length
    val arraySize = if (end == 0) size / length else (size / length) + 1
    return Array(arraySize) {
        val l = if (it == arraySize - 1 && end > 0) end else length
        val array = Array<T?>(l) { null }
        System.arraycopy(this, it * length, this, 0, l)
        array
    }
}

fun ByteArray.split(length: Int): Array<ByteArray> {
    val end = size % length
    val arraySize = if (end == 0) size / length else (size / length) + 1
    return Array(arraySize) {
        val l = if (it == arraySize - 1 && end > 0) end else length
        val array = ByteArray(l)
        System.arraycopy(this, it * length, array, 0, l)
        array
    }
}

fun CharArray.split(length: Int): Array<CharArray> {
    val end = size % length
    val arraySize = if (end == 0) size / length else (size / length) + 1
    return Array(arraySize) {
        val l = if (it == arraySize - 1 && end > 0) end else length
        val array = CharArray(l)
        System.arraycopy(this, it * length, array, 0, l)
        array
    }
}

fun IntArray.split(length: Int): Array<IntArray> {
    val end = size % length
    val arraySize = if (end == 0) size / length else (size / length) + 1
    return Array(arraySize) {
        val l = if (it == arraySize - 1 && end > 0) end else length
        val array = IntArray(l)
        System.arraycopy(this, it * length, array, 0, l)
        array
    }
}

fun FloatArray.split(length: Int): Array<FloatArray> {
    val end = size % length
    val arraySize = if (end == 0) size / length else (size / length) + 1
    return Array(arraySize) {
        val l = if (it == arraySize - 1 && end > 0) end else length
        val array = FloatArray(l)
        System.arraycopy(this, it * length, array, 0, l)
        array
    }
}

fun DoubleArray.split(length: Int): Array<DoubleArray> {
    val end = size % length
    val arraySize = if (end == 0) size / length else (size / length) + 1
    return Array(arraySize) {
        val l = if (it == arraySize - 1 && end > 0) end else length
        val array = DoubleArray(l)
        System.arraycopy(this, it * length, array, 0, l)
        array
    }
}

fun LongArray.split(length: Int): Array<LongArray> {
    val end = size % length
    val arraySize = if (end == 0) size / length else (size / length) + 1
    return Array(arraySize) {
        val l = if (it == arraySize - 1 && end > 0) end else length
        val array = LongArray(l)
        System.arraycopy(this, it * length, array, 0, l)
        array
    }
}