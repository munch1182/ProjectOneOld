package com.munch.lib.android.extend

interface SealedClassToString {
    override fun toString(): String
}

open class SealedClassToStringByName : SealedClassToString {
    override fun toString(): String {
        return javaClass.simpleName
    }
}