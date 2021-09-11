package com.munch.project.one.net

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.base.get
import com.munch.project.one.R
import com.munch.project.one.databinding.ActivityNetBinding

/**
 * Create by munch1182 on 2021/9/1 16:50.
 */
class TestNetActivity : BaseBigTextTitleActivity() {


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
        }
        vm.notice().observe(this) { bind.notice = it }
    }
}