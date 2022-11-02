package com.munch.lib.bluetooth.connect

/**
 * Create by munch1182 on 2022/10/27 18:02.
 */

internal object BluetoothConnectorConfig : IBluetoothHelperConnector {

    internal var builder = BluetoothConnector.Builder()

    /**
     * 对默认连接进行设置
     */
    override fun configConnect(build: BluetoothConnector.Builder.() -> Unit) {
        build.invoke(builder)
    }
}
