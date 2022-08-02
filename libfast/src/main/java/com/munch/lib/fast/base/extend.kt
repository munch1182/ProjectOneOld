@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.fast.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

inline fun LifecycleOwner.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    noinline block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch(context, start, block)
}

inline fun ViewModel.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    noinline block: suspend CoroutineScope.() -> Unit
) {
    viewModelScope.launch(context, start, block)
}