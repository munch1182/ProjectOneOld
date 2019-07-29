package com.munch.module.bluetooth

import android.content.Intent
import android.os.Bundle
import com.munch.lib.log.LogLog
import com.munch.lib.result.ResultHelper
import com.munch.lib.result.ResultListener
import com.munch.lib.test.TestBaseActivity
import com.yscoco.blue.BleManage
import com.yscoco.blue.listener.BleDataListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.experimental.xor

/**
 * Created by Munch on 2019/7/16 8:46
 */
class MainActivity : TestBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LogLog.log((0xAA xor 0X00).toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xDD.toByte())

        ble_btn_connect.setOnClickListener {
            ResultHelper.start4Result(this, BleScanListActivity::class.java, 123)
                .result(object : ResultListener {
                    override fun result(resultCode: Int, intent: Intent) {
                    }
                })
        }
        BleManage.getInstance().mySingleDriver.addBleDataListener(object : BleDataListener {
            override fun read(mac: String?, charUUID: String?, changeByte: ByteArray?) {
                LogLog.log(mac, charUUID, changeByte)
            }

            override fun notify(mac: String?, charUUID: String?, changeByte: ByteArray?) {
                LogLog.log(mac, charUUID, changeByte)
                LogLog.log(mac, charUUID, changeByte!![0].toInt())
            }
        })
        ble_btn_send.setOnClickListener {
            /*val data = ble_et_send.text.toString()
            if (data.isEmpty()) {
                return@setOnClickListener
            }*/

            val arrayOf = arrayOf(
                0xAA.toByte(), 0x02.toByte(),
                0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0xBB.toByte()
            )
            BleManage.getInstance().mySingleDriver.writeData(ByteArray(6) { i -> arrayOf[i] })
        }
        ble_btn_read.setOnClickListener {
            val arrayOf = arrayOf(
                0xAA.toByte(), 0x07.toByte(), 0x00.toByte(), 0x00.toByte(), 0xBB.toByte()
            )
            BleManage.getInstance().mySingleDriver.writeData(ByteArray(5) { i -> arrayOf[i] })
        }
    }
}