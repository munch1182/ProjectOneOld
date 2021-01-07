package com.munch.lib.common

import android.content.Context
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.service.DegradeService
import com.munch.lib.helper.startActivity

@Route(path = "*/*")
class DegradeServiceImp : DegradeService {
    override fun init(context: Context?) {
    }

    override fun onLost(context: Context?, postcard: Postcard?) {
        context?.startActivity(EmptyActivity::class.java)
    }
}