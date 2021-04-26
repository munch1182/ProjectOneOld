package com.munch.pre.lib.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
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

    fun onConnecting(device: BtDevice)

    /**
     * 当连接成功后开始查找服务，在此方法内检查找到的服务
     *
     * @return 返回值会影响最终的连接结果，true则回调连接成功，false则回调连接失败[ConnectFailReason.FILE_FIND_SERVICE]
     */
    fun onDiscoverService(gattService: MutableList<BluetoothGattService>): Boolean {
        return true
    }

    fun onConnectFail(@ConnectFailReason reason: Int)

    fun onConnectSuccess(gatt: BluetoothGatt)

}

/**
 * 状态更改监听
 *
 * @see ConnectState
 */
interface BtConnectStateListener {

    fun onStateChange(@ConnectState newState: Int)

    fun onStateChange(@ConnectState oldState: Int, @ConnectState newState: Int) {}
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
    }
}

@Retention(AnnotationRetention.SOURCE)
annotation class ConnectFailReason {

    companion object {

        const val FILE_CONNECT_BY_SYSTEM = 0
        const val FILE_FIND_SERVICE = 1
    }
}
