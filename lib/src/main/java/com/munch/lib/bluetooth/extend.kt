package com.munch.lib.bluetooth

import androidx.lifecycle.LifecycleOwner
import com.munch.lib.helper.ARSHelper

/**
 * Create by munch1182 on 2021/8/25 16:11.
 */
fun ARSHelper<OnStateChangeListener>.set(owner: LifecycleOwner, onChange: (state: Int) -> Unit) {
    set(owner, object : OnStateChangeListener {
        override fun onChange(state: Int) {
            onChange.invoke(state)
        }
    })
}