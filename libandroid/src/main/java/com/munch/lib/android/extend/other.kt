@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.extend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

inline fun <T> MutableLiveData<T>.immutable(): LiveData<T> = this
inline fun <T> MutableSharedFlow<T>.immutable(): SharedFlow<T> = this
inline fun <T> MutableStateFlow<T>.immutable(): StateFlow<T> = this