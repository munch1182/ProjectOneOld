package com.munch.module.test.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.munch.lib.image.ImageHelper
import com.munch.lib.log.LogLog
import com.munch.module.test.R
import com.munhc.lib.libnative.RootFragment

/**
 * Created by Munch on 2019/7/13 14:28
 */
class Fragment2 : RootFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gif =
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1562996738387&di=83afac842b45b901decc0bcc9a13cb16&imgtype=0&src=http%3A%2F%2Fpic.962.net%2Fup%2F2018-5%2F15262691097592176.gif"
        ImageHelper.res(gif).into(view.findViewById(R.id.iv))
    }

}