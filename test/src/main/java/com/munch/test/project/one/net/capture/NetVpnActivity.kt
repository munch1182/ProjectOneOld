package com.munch.test.project.one.net.capture

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import com.munch.pre.lib.helper.ThreadPoolHelper
import com.munch.pre.lib.helper.file.closeQuietly
import com.munch.pre.lib.log.Logger
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityNetVpnBinding
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.util.concurrent.ThreadPoolExecutor

/**
 * Create by munch1182 on 2021/5/5 16:33.
 */
class NetVpnActivity : BaseTopActivity() {

    private val bind by bind<ActivityNetVpnBinding>(R.layout.activity_net_vpn)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            lifecycleOwner = this@NetVpnActivity
            netCaptureStart.setOnClickListener {
                startOrStopVpn()
                if (VpnTestService.isRunning) {
                    netCaptureStart.text = "stop"
                } else {
                    netCaptureStart.text = "start"
                }
            }
        }
    }

    private fun startOrStopVpn() = VpnTestService.startOrStop(this)

    class VpnTestService : VpnService(), Runnable {

        companion object {

            var isRunning = false
            private const val REQUEST_CODE_FOR_VPN = 55
            private val log = Logger().apply {
                tag = "VpnTest"
                noStack = true
            }
            private const val IP_LOCAL = "10.8.0.2"
            private const val PORT = 55

            fun startOrStop(context: Activity) {
                val intent = prepare(context)
                log.log("prepare: ${intent == null}")
                if (intent != null) {
                    context.startActivityForResult(intent, REQUEST_CODE_FOR_VPN)
                } else {
                    log.log("isRunning: $isRunning")
                    if (isRunning) {
                        context.stopService(Intent(context, VpnTestService::class.java))
                    } else {
                        context.startService(Intent(context, VpnTestService::class.java))
                    }
                    isRunning = !isRunning
                }
            }
        }

        private lateinit var pool: ThreadPoolExecutor

        override fun onCreate() {
            super.onCreate()
            log.log("onCreate")
            pool = ThreadPoolHelper.newFixThread()
            pool.execute(this)
        }

        override fun onDestroy() {
            super.onDestroy()
            pool.shutdownNow()
            log.log("onDestroy")
        }

        override fun run() {

        }
    }

    class ProxyService(port: Int) : Runnable {
        private val selector: Selector? by lazy { Selector.open() }
        private val service: ServerSocketChannel? by lazy {
            ServerSocketChannel.open().apply {
                configureBlocking(false)
                socket().bind(InetSocketAddress(port))
                register(selector, SelectionKey.OP_ACCEPT)
            }
        }

        fun start() {
            Thread(this, "").start()
        }

        fun stop() {
            selector.closeQuietly()
            service.closeQuietly()
        }

        override fun run() {
        }
    }
}