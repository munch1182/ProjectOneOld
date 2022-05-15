package com.munch.project.one.net

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.munch.lib.extend.get
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.fvFv
import com.munch.lib.fast.view.supportDef
import com.munch.lib.helper.NetHelper
import com.munch.lib.helper.getName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by munch1182 on 2022/5/15 20:32.
 */
class NetActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val bind by fvFv(arrayOf("set static ip"))
    private val vm by get<NetViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.init()
        NetHelper.instance.observe(this) {
            lifecycleScope.launch {
                val address = withContext(Dispatchers.IO) { NetHelper.instance.getNetAddress() }
                bind.desc("curr: $it, ${it?.getName()}, $address")
            }
        }
        bind.click { _, index ->
            when (index) {
                0 -> setStaticIp()
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val s = vm.queryNotAvailableAddress()?.joinToString() ?: "null"
            withContext(Dispatchers.Main) { bind.desc(s) }
        }
    }

    private fun setStaticIp() {
        getAvailableAddress()
    }

    private fun getAvailableAddress() {

    }
}