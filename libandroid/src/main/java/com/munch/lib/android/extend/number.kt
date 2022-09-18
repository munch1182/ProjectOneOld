@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

inline fun Int.hasFlag(flag: Int): Boolean = this and flag == flag