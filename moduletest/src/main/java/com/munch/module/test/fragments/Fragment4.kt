package com.munch.module.test.fragments

import com.munhc.lib.libnative.root.RootFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.munch.lib.image.ImageHelper
import com.munch.module.test.R
/**
 * Created by Munch on 2019/7/13 14:28
 */
class Fragment4 : RootFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gif =
            "http://img3.duitang.com/uploads/item/201607/21/20160721222321_aTJmv.thumb.700_0.jpeg"
        ImageHelper.res(gif).into(view.findViewById(R.id.iv))
    }
}