package com.munch.module.test.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.munch.lib.image.ImageHelper
import com.munch.module.test.R
import com.munhc.lib.libnative.root.RootFragment

/**
 * Created by Munch on 2019/7/13 14:28
 */
class Fragment3 : RootFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gif =
            "http://gss0.baidu.com/7Po3dSag_xI4khGko9WTAnF6hhy/zhidao/pic/item/f703738da9773912445bbabcff198618377ae2ea.jpg"
        ImageHelper.res(gif).into(view.findViewById(R.id.iv))
    }
}