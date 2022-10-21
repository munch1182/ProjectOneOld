package com.munch.lib.bluetooth

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Create by munch1182 on 2022/10/21 17:48.
 */
fun BluetoothHelper.set(owner: LifecycleOwner, listener: OnBluetoothDevScannedListener) {
    owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            addScanListener(listener)
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            removeScanListener(listener)
        }
    })
}