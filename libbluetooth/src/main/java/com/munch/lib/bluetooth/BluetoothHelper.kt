package com.munch.lib.bluetooth

import com.munch.lib.android.extend.ScopeContext
import com.munch.lib.bluetooth.env.BluetoothEnv
import com.munch.lib.bluetooth.env.IBluetoothManager
import com.munch.lib.bluetooth.env.IBluetoothState
import com.munch.lib.bluetooth.helper.*

/**
 * 给蓝牙相关提供环境对象、更改监听、扫描、连接和数据发送相关功能
 *
 * Create by munch1182 on 2022/9/29 14:25.
 */
object BluetoothHelper : ScopeContext,
    IBluetoothHelperEnv by BluetoothHelperEnv,
    IBluetoothManager by BluetoothEnv,
    IBluetoothState by BluetoothEnv,
    IBluetoothHelperScanner by BluetoothHelperImpScanner(),
    IBluetoothHelperConfig by BluetoothHelperConfig {


//    fun send(dev: BluetoothDev, byteArray: ByteArray) {
//
//    }
}