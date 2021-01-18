package com.munch.lib.helper

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * Create by munch1182 on 2021/1/15 17:43.
 */

inline fun LifecycleOwner.obWhenDestroy(crossinline onDestroy: () -> Unit) {
    lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            onDestroy.invoke()
            removeOb(this)
        }
    })
}

fun LifecycleOwner.removeOb(observer: LifecycleObserver) {
    lifecycle.removeObserver(observer)
}


inline fun LifecycleOwner.obWhenCreate(
    crossinline onCreate: () -> Unit,
    crossinline onDestroy: () -> Unit
) {
    lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreate() {
            onCreate.invoke()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            onDestroy.invoke()
            removeOb(this)
        }
    })
}


@JvmOverloads
inline fun LifecycleOwner.obWhenStart(
    crossinline onStart: () -> Unit,
    crossinline onStop: () -> Unit,
    noinline onDestroy: (() -> Unit)? = null
) {
    lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            onStart.invoke()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            onStop.invoke()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            onDestroy?.invoke()
            removeOb(this)
        }
    })
}

@JvmOverloads
inline fun LifecycleOwner.obWhenResume(
    crossinline onResume: () -> Unit,
    crossinline onPause: () -> Unit,
    noinline onDestroy: (() -> Unit)? = null
) {
    lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            onResume.invoke()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            onPause.invoke()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            onDestroy?.invoke()
            removeOb(this)
        }
    })
}