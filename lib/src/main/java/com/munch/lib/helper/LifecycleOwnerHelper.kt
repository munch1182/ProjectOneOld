package com.munch.lib.helper

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * Create by munch1182 on 2021/1/15 17:43.
 */

fun Any.obWhenDestroy(owner: LifecycleOwner?, onDestroy: Any.() -> Unit) {
    owner ?: return
    owner.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            onDestroy.invoke(this@obWhenDestroy)
            removeOb(owner, this)
        }
    })
}

fun removeOb(owner: LifecycleOwner, observer: LifecycleObserver) {
    owner.lifecycle.removeObserver(observer)
}

@JvmOverloads
fun Any.obWhenCreate(
    owner: LifecycleOwner?,
    onCreate: Any.() -> Unit,
    onDestroy: (Any.() -> Unit)? = null
) {
    owner ?: return
    owner.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreate() {
            onCreate.invoke(this@obWhenCreate)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            onDestroy?.invoke(this@obWhenCreate)
            removeOb(owner, this)
        }
    })
}

@JvmOverloads
fun Any.obWhenStart(
    owner: LifecycleOwner?,
    onStart: Any.() -> Unit,
    onStop: (Any.() -> Unit)? = null,
    onDestroy: (Any.() -> Unit)? = null
) {
    owner ?: return
    owner.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            onStart.invoke(this@obWhenStart)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            onStop?.invoke(this@obWhenStart)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            onDestroy?.invoke(this@obWhenStart)
            removeOb(owner, this)
        }
    })
}

@JvmOverloads
fun Any.obWhenResume(
    owner: LifecycleOwner?,
    onResume: Any.() -> Unit,
    onPause: (Any.() -> Unit)? = null,
    onDestroy: (Any.() -> Unit)? = null
) {
    owner ?: return
    owner.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onStart() {
            onResume.invoke(this@obWhenResume)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onStop() {
            onPause?.invoke(this@obWhenResume)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            onDestroy?.invoke(this@obWhenResume)
            removeOb(owner, this)
        }
    })
}

