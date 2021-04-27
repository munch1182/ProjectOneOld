package com.munch.pre.lib.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import androidx.annotation.IntDef
import com.munch.pre.lib.ATTENTION

/**
 * Create by munch1182 on 2021/4/26 16:54.
 */

/**
 * 连接回调
 */
interface BtConnectListener {

    fun onStart(device: BtDevice)

    fun onConnectFail(device: BtDevice, @ConnectFailReason reason: Int)

    fun onConnectSuccess(device: BtDevice, gatt: BluetoothGatt)
}

interface BtConnectFailListener : BtConnectListener {

    override fun onStart(device: BtDevice) {
    }

    override fun onConnectSuccess(device: BtDevice, gatt: BluetoothGatt) {
    }
}

/**
 * 状态更改监听
 *
 * @see ConnectState
 */
interface BtConnectStateListener {

    fun onStateChange(@ConnectState oldState: Int, @ConnectState newState: Int)
}


@IntDef(
    ConnectState.STATE_CONNECTING,
    ConnectState.STATE_CONNECTED,
    ConnectState.STATE_DISCONNECTING,
    ConnectState.STATE_DISCONNECTED
)
@Retention(AnnotationRetention.SOURCE)
annotation class ConnectState {

    companion object {
        const val STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING
        const val STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED
        const val STATE_DISCONNECTING = BluetoothProfile.STATE_DISCONNECTING
        const val STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED

        @ATTENTION
        @ConnectState
        fun from(state: Int): Int {
            return state
        }

        fun isConnected(state: Int): Boolean {
            return state == STATE_CONNECTED
        }

        fun unConnected(state: Int): Boolean {
            return state == STATE_DISCONNECTED
        }
    }
}

@IntDef(
    ConnectFailReason.FAIL_CONNECT_BY_SYSTEM,
    ConnectFailReason.FAIL_FIND_SERVICE,
    ConnectFailReason.FAIL_REQUEST_MTU,
    ConnectFailReason.FAIL_WRITE_DESCRIPTOR,
)
@Retention(AnnotationRetention.SOURCE)
annotation class ConnectFailReason {

    companion object {

        const val FAIL_CONNECT_BY_SYSTEM = 0
        const val FAIL_FIND_SERVICE = 1
        const val FAIL_REQUEST_MTU = 2
        const val FAIL_WRITE_DESCRIPTOR = 3
        const val FAIL_READ_DESCRIPTOR = 3
    }
}
