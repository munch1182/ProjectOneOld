package com.munch.lib.bluetooth

import com.munch.lib.android.extend.ScopeContext

/**
 * 给蓝牙相关提供环境对象、更改监听、扫描、连接和数据发送相关功能
 *
 * 对于连接和数据发送相关操作, 也可使用[BluetoothDev]对象直接操作
 *
 * 因为[BluetoothEnv]和[BluetoothHelperScanner]先于[BluetoothHelper]初始化, 因此, 这些类不能使用[BluetoothHelper]对象
 *
 * Create by munch1182 on 2022/9/29 14:25.
 */
object BluetoothHelper : ScopeContext,
    IBluetoothHelperEnv by BluetoothHelperEnv,
    IBluetoothManager by BluetoothEnv,
    IBluetoothState by BluetoothEnv,
    IBluetoothHelperScanner by BluetoothHelperScanner