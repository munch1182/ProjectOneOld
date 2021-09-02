package com.munch.lib.bluetooth

import androidx.annotation.IntDef
import com.munch.lib.base.OnChangeListener

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

interface OnStateChangeListener {

    fun onChange(@BluetoothState state: Int)
}

class BluetoothStateHelper {

    private val lock = Object()
    internal var onChangeListener: OnChangeListener? = null

    @BluetoothState
    internal var currentStateVal: Int = BluetoothState.CLOSE
        get() = synchronized(lock) { field }
        set(value) {
            synchronized(lock) {
                if (field == value && lastStateVal != -1) {
                    return@synchronized
                }
                val lastState = field
                field = value
                //lastStateVal为-1时是初始化的时候
                if (lastStateVal != -1) {
                    BluetoothHelper.logHelper.withEnable { "state: $lastState -> $value" }
                    onChangeListener?.onChange()
                }
                lastStateVal = lastState
            }
        }

    private var lastStateVal = -1

    @BluetoothState
    val lastState: Int
        get() = lastStateVal

    @BluetoothState
    val currentState: Int
        get() = currentStateVal

    val isScanning: Boolean
        get() = currentStateVal == BluetoothState.SCANNING

    val isConnected: Boolean
        get() = currentStateVal == BluetoothState.CONNECTED

    val isConnecting: Boolean
        get() = currentStateVal == BluetoothState.CONNECTING

    val isClose: Boolean
        get() = currentStateVal == BluetoothState.CLOSE

    val isIdle: Boolean
        get() = currentStateVal == BluetoothState.IDLE

    private val isStop: Boolean
        get() = isIdle || isClose

    val isScanComplete: Boolean
        get() = lastStateVal == BluetoothState.SCANNING && isStop

    val isDisconnect: Boolean
        get() = lastStateVal == BluetoothState.CONNECTED && isStop

}