package com.munch.lib.bluetooth.connect

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.helper.BluetoothHelperConfig
import com.munch.lib.bluetooth.helper.BluetoothHelperEnv
import com.munch.lib.bluetooth.helper.IBluetoothHelperEnv
import kotlinx.coroutines.delay
import kotlin.coroutines.resume

/**
 * Create by munch1182 on 2022/10/27 18:02.
 */

/**
 * 此类只实现IBluetoothConnectFun, 不实现状态更改, 因此调用此类的方法时需要先自行判断状态
 */
internal abstract class BluetoothConnectFun(protected val dev: BluetoothDevice) :
    IBluetoothConnectFun,
    IBluetoothHelperEnv by BluetoothHelperEnv {

    companion object {
        private const val TAG = "conn"
    }

    protected fun log(content: String) {
        if (enableLog) {
            log.log("[$TAG]: [$dev]: $content")
        }
    }
}

internal class BluetoothLeConnectFun(
    dev: BluetoothDevice,
    private val gatt: BluetoothGattHelper
) : BluetoothConnectFun(dev) {

    private var _gatt: BluetoothGatt? = null

    override suspend fun connect(
        timeout: Long,
        config: BluetoothConnector.Config?
    ): BluetoothConnectResult {
        val b = config ?: BluetoothHelperConfig.config.connectConfig
        val result: BluetoothConnectResult =
            com.munch.lib.android.extend.suspendCancellableCoroutine(timeout) {
                val onConnect = object : BluetoothGattHelper.OnConnectStateChangeListener {
                    override fun onConnectStateChange(status: Int, newState: Int) {
                        when (newState) {
                            BluetoothProfile.STATE_CONNECTED -> {
                                gatt.remove(this)
                                if (it.isActive) it.resume(BluetoothConnectResult.Success)
                            }
                            BluetoothProfile.STATE_DISCONNECTED -> {
                                gatt.remove(this)
                                val reason =
                                    BluetoothConnectFailReason.SysErr(status).toReason()
                                if (enableLog) log("connect gatt: fail : $reason")
                                if (it.isActive) it.resume(reason)
                            }
                            else -> {}  //wait
                        }
                    }

                }
                gatt.add(onConnect)
                if (enableLog) log("start CONNECT gatt")
                _gatt = dev.connectGatt(null, false, gatt.callback, b.transport, b.phy)
            } ?: BluetoothConnectFailReason.ConnectTimeout.toReason()
        if (enableLog) log("connect result: $result")
        return result
    }

    override suspend fun disconnect(removeBond: Boolean): Boolean {
        if (_gatt != null) {
            log("start DISCONNECT gatt")
            _gatt?.disconnect()
            var index = 5
            while (index > 0) {
                delay(200L)
                if (BluetoothHelper.isConnect(dev) == false) {
                    break
                }
                index--
            }
        }
        return true
    }
}

internal class BluetoothClassicConnectFun(dev: BluetoothDevice) : BluetoothConnectFun(dev) {
    override suspend fun connect(
        timeout: Long,
        config: BluetoothConnector.Config?
    ): BluetoothConnectResult {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect(removeBond: Boolean): Boolean {
        TODO("Not yet implemented")
    }

}
