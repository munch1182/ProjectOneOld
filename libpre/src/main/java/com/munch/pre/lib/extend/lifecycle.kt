package com.munch.pre.lib.extend

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * LifecycleOwner的相关方法晚于activity的相关方法执行
 *
 * Create by munch1182 on 2021/4/1 15:22.
 */
inline fun LifecycleOwner.obOnResume(
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

inline fun LifecycleOwner.obOnDestroy(crossinline onDestroy: () -> Unit) {
    lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            onDestroy.invoke()
            removeOb(this)
        }
    })
}

inline fun LifecycleOwner.obOnCreate(
    crossinline onCreate: () -> Unit,
    noinline onDestroy: (() -> Unit)? = null
) {
    lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreate() {
            onCreate.invoke()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            onDestroy?.invoke()
            removeOb(this)
        }
    })
}

inline fun LifecycleOwner.obOnStart(
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

fun LifecycleOwner.removeOb(observer: LifecycleObserver) {
    lifecycle.removeObserver(observer)
}