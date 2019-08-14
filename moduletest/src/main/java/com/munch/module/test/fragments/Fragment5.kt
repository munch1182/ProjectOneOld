package com.munch.module.test.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.munch.lib.image.ImageLoadHelper
import com.munch.lib.libnative.root.RootFragment
import com.munch.module.test.R

/**
 * Created by Munch on 2019/7/13 14:28
 */
class Fragment5 : RootFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gif =
            "http://b-ssl.duitang.com/uploads/item/201805/22/20180522084756_mmmla.jpeg"
        ImageLoadHelper.res(gif).into(view.findViewById(R.id.iv))
    }
}