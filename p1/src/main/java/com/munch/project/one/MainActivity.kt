package com.munch.project.one

import android.os.Bundle
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.SupportActionBar
import com.munch.lib.fast.view.fvLineRv
import com.munch.lib.recyclerview.setOnItemClickListener

class MainActivity : BaseFastActivity(), SupportActionBar {

    private val vb by fvLineRv(
        Array(10){it.toString()}.toList()
    )
    override val showHome = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vb.adapter.setOnItemClickListener { _, pos, _ ->
            toast("$pos")
        }

        title = "123"
    }
}