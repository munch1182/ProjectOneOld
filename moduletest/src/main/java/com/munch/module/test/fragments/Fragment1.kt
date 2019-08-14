package com.munch.module.test.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.munch.lib.image.ImageLoadHelper
import com.munch.lib.libnative.root.RootFragment
import com.munch.lib.log.LogLog
import com.munch.module.test.MainActivity
import com.munch.module.test.R
import com.munch.module.test.ResultActivity

/**
 * Created by Munch on 2019/7/13 14:28
 */
class Fragment1 : RootFragment() {
    private val KEY_INPUT = "111"
    private var et: EditText? = null
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        LogLog.log(hidden)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LogLog.log()
        val view = inflater.inflate(R.layout.fragment1, container, false)
        et = view.findViewById<EditText>(R.id.et)
        /*et?.setText(savedInstanceState?.getString(KEY_INPUT))*/
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogLog.log(savedInstanceState)
        val res = "http://www.ratoo.net/uploads/allimg/170823/15-1FR31H604.jpg"
        val targetView = view.findViewById<View>(R.id.iv)
        ImageLoadHelper.res(res).transformation(CenterCrop()).into(targetView)
        targetView.setOnClickListener { startActivity(ResultActivity::class.java) }
        view.findViewById<View>(R.id.btn).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                (activity!! as MainActivity).setTrans()
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        LogLog.log(isVisibleToUser)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogLog.log()
    }

    override fun onStart() {
        super.onStart()
        LogLog.log()
    }

    override fun onStartLoad() {
        super.onStartLoad()
        LogLog.log("====================================")
    }

    override fun onStopLoad() {
        super.onStopLoad()
        LogLog.log("/////////////////////////////////////")
    }

    override fun onResume() {
        super.onResume()
        LogLog.log()
    }

    override fun onPause() {
        super.onPause()
        LogLog.log()
    }

    override fun onStop() {
        super.onStop()
        LogLog.log()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_INPUT, et?.text.toString())
        /*LogLog.log(outState)*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        et = null
        LogLog.log(et)
        LogLog.log(1111)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogLog.log()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        LogLog.log()
    }

    override fun onDetach() {
        super.onDetach()
        LogLog.log()
    }
}