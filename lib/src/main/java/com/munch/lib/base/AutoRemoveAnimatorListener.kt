package com.munch.lib.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.helper.obWhenResume

/**
 * 自动在动画结束的时候删除listener
 * Create by munch1182 on 2021/2/27 14:27.
 */
open class AutoRemoveAnimatorListener : AnimatorListenerAdapter() {

    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
        super.onAnimationEnd(animation, isReverse)
        animation?.removeAllListeners()
    }
}

/**
 * 在声明周期暂停时同时暂停动画，恢复时自动恢复动画
 *
 * 一般动画不需要
 */
fun Animator?.removeAllWhenEnd(
    autoRemove: AutoRemoveAnimatorListener = AutoRemoveAnimatorListener(),
    owner: LifecycleOwner
) {
    this ?: return
    owner.obWhenResume({
        if (isPaused) {
            resume()
        }
    }, onPause = {
        if (isRunning) {
            pause()
        }
    }, onDestroy = {
        if (isRunning || isPaused) {
            cancel()
        }
    })
    addListener(autoRemove)
}

fun Animator?.removeAllWhenEnd(autoRemove: AutoRemoveAnimatorListener = AutoRemoveAnimatorListener()) {
    this ?: return
    addListener(autoRemove)
}