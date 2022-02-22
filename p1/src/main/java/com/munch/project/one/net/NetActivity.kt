package com.munch.project.one.net

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.lifecycle.lifecycleScope
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.base.get
import com.munch.lib.helper.ExecHelper
import com.munch.lib.log.log
import com.munch.project.one.R
import com.munch.project.one.databinding.ActivityNetBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/9/1 16:50.
 */
class NetActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityNetBinding>(R.layout.activity_net)
    private val vm by get(NetViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            notice = vm.notice().toString()
            netBtnWifi.setOnClickListener {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
            netBtnCellular.setOnClickListener { startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)) }
            netBtnTcpip.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        log(ExecHelper.exec("tcpip 5555"))
                        toast("tcpip success")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        toast("tcpip fail")
                    }
                }
            }
        }
        vm.notice().observe(this) { bind.notice = it }
    }
}