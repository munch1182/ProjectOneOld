package com.munch.lib.fast.base

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.base.startActivity
import com.munch.lib.fast.R
import com.munch.lib.fast.databinding.ItemSimpleTvBinding
import com.munch.lib.fast.recyclerview.SimpleAdapter
import com.munch.lib.fast.recyclerview.setOnItemClickListener

/**
 *
 * 简单实现了RecyclerView布局的activity
 *
 * Create by munch1182 on 2021/8/10 11:44.
 */
abstract class BaseRvActivity : BaseBigTextTitleActivity() {

    protected open val simpleAdapter by lazy {
        SimpleAdapter<String, ItemSimpleTvBinding>(R.layout.item_simple_tv, getData())
        { _, bind, str -> bind.text = str }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_rv_only)

        findViewById<RecyclerView>(R.id.rv_view).apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = simpleAdapter
        }
        simpleAdapter.setOnItemClickListener { _, pos, _ -> onClick(pos) }
    }

    protected open fun getData(): MutableList<String>? =
        targets?.map { it.simpleName.replace("Activity", "") }
            ?.toMutableList()

    protected open fun onClick(pos: Int) {
        val activity = targets?.get(pos) ?: return
        startActivity(activity)
    }

    protected open val targets: MutableList<Class<out BaseActivity>>? = null
}