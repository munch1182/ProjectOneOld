package com.munch.plugin.android.helper


fun <T> catch(any: () -> T?): T? {
    return try {
        any.invoke()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun log(any: Any?) {
    println(any?.toString() ?: "null")
}