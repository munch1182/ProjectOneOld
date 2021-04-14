package com.munch.test.project.one.net

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.fast.base.BaseBindAdapter
import com.munch.lib.fast.base.BaseBindViewHolder
import com.munch.lib.fast.databinding.ItemBaseTopTvBinding
import com.munch.pre.lib.base.rv.DiffItemCallback
import com.munch.pre.lib.helper.NetStatusHelper
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityNetIpBinding
import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch

/**
 * Create by munch1182 on 2021/4/14 10:56.
 */
@SuppressLint("SetTextI18n")
class NetIpActivity : BaseTopActivity() {

    private val bind by bind<ActivityNetIpBinding>(R.layout.activity_net_ip)
    private val itemAdapter by lazy {
        object : BaseBindAdapter<String, ItemBaseTopTvBinding>(R.layout.item_base_top_tv) {

            init {
                diffUtil = object : DiffItemCallback<String>() {
                    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                        return oldItem == newItem
                    }
                }
            }

            override fun onBindViewHolder(
                holder: BaseBindViewHolder<ItemBaseTopTvBinding>, bean: String, pos: Int
            ) {
                holder.bind.itemBaseTopTv.text = bean
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            lifecycleOwner = this@NetIpActivity
            netIpBtn.setOnClickListener {
                this.netIpBtn.text = "IP: ${NetStatusHelper.getIpAddress() ?: "null"}"
            }
            netIpRv.apply {
                layoutManager = LinearLayoutManager(this@NetIpActivity)
                adapter = itemAdapter
            }
            netIpBtnAll.setOnClickListener { scanIp() }
        }
    }

    private fun scanIp() {
        itemAdapter.set(mutableListOf())
        if (!isWifiEnable()) {
            toast("wifi不可用")
            return
        }
        val selfIp = NetStatusHelper.getIpAddress()
        if (selfIp == null) {
            toast("无法获取自身ip")
            return
        }
        val ipSuffix = selfIp.subSequence(0, selfIp.lastIndexOf("."))
        val cd by lazy { CountDownLatch(255) }
        bind.netIpSrl.isRefreshing = true
        val list = mutableListOf<String>()
        lifecycleScope.launch(Dispatchers.IO) {
            for (i in 1..255) {
                val ip = "$ipSuffix.$i"
                if (ip == selfIp) {
                    cd.countDown()
                    continue
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    var process: Process? = null
                    try {
                        //-c 1为发送的次数，-w 表示发送后等待响应的时间
                        process = Runtime.getRuntime().exec("ping -c 1 -w 3 $ip")
                        val res = process.waitFor()
                        if (res == 0) {
                            list.add(ip)
                        }
                        cd.countDown()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        process?.destroy()
                    }
                }
            }
            cd.await()
            if (list.isNotEmpty()) {
                list.sortWith { o1, o2 ->
                    if (o1 == o2) return@sortWith 0
                    o1 ?: return@sortWith -1
                    o2 ?: return@sortWith 1
                    fun last(str: String) = str.split(".")[3].toInt()
                    return@sortWith last(o1) - last(o2)
                }
            }
            withContext(Dispatchers.Main) {
                itemAdapter.set(list)
                bind.netIpSrl.postDelayed({ bind.netIpSrl.isRefreshing = false }, 500L)
            }
        }
    }

    private fun isWifiEnable(): Boolean {
        return (application.getSystemService(Context.WIFI_SERVICE) as WifiManager?)?.isWifiEnabled
            ?: false
    }
}