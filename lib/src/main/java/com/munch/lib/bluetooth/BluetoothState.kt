package com.munch.lib.bluetooth

import androidx.annotation.IntDef

/**
 *
 * 说明当前蓝牙的状态
 *
 * Create by munch1182 on 2021/8/24 14:07.
 */
@IntDef(
    BluetoothState.IDLE,
    BluetoothState.SCANNING,
    BluetoothState.CONNECTING,
    BluetoothState.CONNECTED,
    BluetoothState.CLOSE,
)
@Retention(AnnotationRetention.SOURCE)
annotation class BluetoothState {

    companion object {
        /**
         * 已打开但是未涉及操作
         */
        const val IDLE = 0

        /**
         * 正在扫描中
         */
        const val SCANNING = 1

        /**
         * 正在连接中
         */
        const val CONNECTING = 2

        /**
         * 已连接
         */
        const val CONNECTED = 3

        /**
         * 蓝牙已关闭
         */
        const val CLOSE = 4
    }
}