package com.munch.lib.fast.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.setPadding
import com.munch.lib.base.OnViewIndexClickListener
import com.munch.lib.fast.R
import com.munch.lib.weight.FlowLayout

/**
 * 将btn横向流式布局的activity
 *
 * Create by munch1182 on 2021/8/10 17:29.
 */
open class BaseBtnFlowActivity : BaseBigTextTitleActivity() {

    private val flowLayout by lazy {
        FlowLayout(this).apply { setPadding(16) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onItemClick = object : OnViewIndexClickListener {
            override fun onClick(v: View?, index: Int) {
                onClick(index)
            }
        }
        setContentView(flowLayout.apply {
            val li = LayoutInflater.from(this.context)
            getData()?.forEachIndexed { index, s ->
                this.addView(li.inflate(R.layout.item_simple_btn, this, false).apply {
                    this.findViewById<TextView>(R.id.item_btn_view)?.text = s
                    this.tag = index
                    setOnClickListener(onItemClick)
                })
            }
        })
    }

    protected open fun getData(): MutableList<String>? = null
    protected open fun onClick(pos: Int) {}
}