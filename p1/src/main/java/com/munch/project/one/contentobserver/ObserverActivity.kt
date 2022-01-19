package com.munch.project.one.contentobserver

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.log.LogLog
import com.munch.lib.log.log
import com.munch.lib.result.contactWith
import com.munch.lib.result.with
import com.munch.project.one.contentobserver.handler.CallHandler
import com.munch.project.one.contentobserver.handler.CallStateHandler
import com.munch.project.one.contentobserver.handler.SmsStateHandler
import com.munch.project.one.databinding.ActivityObserverBinding

/**
 * Create by munch1182 on 2021/10/22 09:47.
 */
class ObserverActivity : BaseBigTextTitleActivity() {

    private lateinit var th: HandlerThread
    private lateinit var handler: Handler

    private val bind by bind<ActivityObserverBinding>()
    private val call by lazy { CallHandlerImp() }
    private val sms by lazy { SmsHandlerImp() }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.observerBtnCall.setOnClickListener { enableCall() }
        bind.observerBtnSms.setOnClickListener { enableSms() }

        bind.observerBtnAnswer.setOnClickListener { catch { call.answer() } }
        bind.observerBtnRefuse.setOnClickListener { catch { call.refuse() } }
        bind.observerBtnMute.setOnClickListener { catch { call.mute() } }

        th = HandlerThread("ContentObserver")
        th.start()
        handler = Handler(th.looper)
    }

    private fun catch(func: () -> Unit) {
        try {
            func.invoke()
        } catch (e: Exception) {
            log(e)
        }
    }

    private fun enableSms() {
        sms.start()
    }

    private fun enableCall() {
        call.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            call.stopObserve()
            sms.stopObserve()
        } catch (e: Exception) {
        }
    }

    @SuppressLint("SetTextI18n")
    private inner class CallHandlerImp :
        CallHandler(this, handler, object : CallStateHandler.OnCallListener {
            override fun onCall(number: String?) {
                runOnUiThread { bind.observerTvCall.text = "call: $number" }
            }

            override fun onAnswer() {
                runOnUiThread { bind.observerTvCall.text = "call: answer" }
            }

            override fun onRefuse() {
                runOnUiThread { bind.observerTvCall.text = "call: refuse" }
            }

            override fun onMissPhone(number: String?, read: Boolean) {
                runOnUiThread { bind.observerTvCall.text = "call: miss $number, read:$read" }
            }
        }, LogLog) {

        override fun requestPermission() {
            super.requestPermission()
            log("requestPermission")
            contactWith(*permissions)
                .contactWith(
                    { hadNotificationPolicy(this@ObserverActivity) }, notificationPolicyIntent
                )
                .start {
                    if (it) {
                        onPermissionGranted()
                    } else {
                        toast("没有权限")
                    }
                }
        }

        @SuppressLint("SetTextI18n")
        override fun onPermissionGranted() {
            super.onPermissionGranted()
            runOnUiThread { bind.observerTvSms.text = "call state handle" }
        }
    }

    private inner class SmsHandlerImp :
        SmsStateHandler(this, handler, object : OnSmsReceiveListener {
            @SuppressLint("SetTextI18n")
            override fun onSmsReceived(id: Int, number: String, content: String) {
                runOnUiThread {
                    bind.observerTvSms.text = "sms received: $number, id:$id, $content"
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onSmsRead(id: Int) {
                runOnUiThread { bind.observerTvSms.text = "sms read: id:$id" }
            }
        },LogLog) {

        override fun requestPermission() {
            super.requestPermission()
            with(*permissions)
                .request {
                    if (it) {
                        onPermissionGranted()
                    } else {
                        toast("没有权限")
                    }
                }
        }

        @SuppressLint("SetTextI18n")
        override fun onPermissionGranted() {
            super.onPermissionGranted()
            runOnUiThread { bind.observerTvSms.text = "sms state handle" }
        }
    }
}