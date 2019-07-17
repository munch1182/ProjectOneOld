package com.munch.module.bluetooth

import com.munch.lib.test.app.TestApp
import com.yscoco.blue.BleConfig
import com.yscoco.blue.BleManage
import com.yscoco.blue.bean.NotifyUUIDBean

/**
 * Created by Munch on 2019/7/16 9:08
 */
class App : TestApp() {

    companion object {

        private var app: App? = null

        fun getInstance() = app!!
    }

    override fun onCreate() {
        super.onCreate()

        app = this

        initBluetooth()
    }

    private fun initBluetooth() {
        BleManage.getInstance().init(this,
            BleConfig().apply {
                SERVICE_UUID1 = BluetoothConstant.SERVICE_UUID1
                CHA_NOTIFY = BluetoothConstant.CHA_NOTIFY
                CHA_WRITE = BluetoothConstant.CHA_WRITE
                setNotifyList(arrayListOf(NotifyUUIDBean(SERVICE_UUID1, CHA_NOTIFY)))
            })
    }

    fun getBleDriver() = BleManage.getInstance().mySingleDriver
}