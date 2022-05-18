package com.munch.lib.helper

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Created by munch1182 on 2022/5/18 23:43.
 */
interface RegisterHelper<T> {

    fun register(t: T?)
    fun unregister(t: T? = null)

    /**
     * 在调用的时候添加，在onDestroy的时候移除
     */
    fun set(owner: LifecycleOwner, t: T) {
        register(t)
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                owner.lifecycle.removeObserver(this)
                unregister(t)
            }
        })
    }

    /**
     * 在onResume的时候添加，在onPause的时候移除
     */
    fun setWhenResume(owner: LifecycleOwner, t: T) {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                register(t)
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                unregister(t)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    /**
     * 在onStart的时候添加，在onStop的时候移除
     */
    fun setWhenStart(owner: LifecycleOwner, t: T) {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onResume(owner)
                register(t)
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onPause(owner)
                unregister(t)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                owner.lifecycle.removeObserver(this)
            }
        })
    }
}