package com.munch.project.one.recyclerview

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.android.AppHelper
import com.munch.lib.android.extend.*
import com.munch.lib.android.recyclerview.BaseViewHolder
import com.munch.lib.android.recyclerview.SimpleBaseViewAdapter
import com.munch.lib.fast.R
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.dispatch.SupportBindConfigDialog
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.base.dispatchDef
import com.munch.project.one.databinding.ConfigRecyclerviewBinding
import com.munch.project.one.recyclerview.RecyclerIntent as Intent
import com.munch.project.one.recyclerview.RecyclerState as State

class RecyclerViewActivity : BaseActivity(), ActivityDispatch by dispatchDef(ConfigRecyclerView()) {

    private val vm by get<RecyclerVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rv = RecyclerView(this)
        setContentView(rv)

        val lm = LinearLayoutManager(this)
        rv.layoutManager = lm
        rv.addItemDecoration(LinearLineItemDecoration())
        val showAdapter = ShowAdapter()
        rv.adapter = showAdapter

        val scroller = TopScroller(lm)
        showAdapter.moveAfterAdd { scroller.targetPosition = it }
            .setOnItemClick { showAdapter.remove(it.pos) } // adapter自己实现 remove
            .setOnItemLongClick { vm.dispatch(Intent.Update(it.pos)) } // Update交由vm来实现

        vm.state.observe(this) {
            when (it) {
                State.Loading -> {} // todo
                State.None -> {} // todo
                is State.Data -> showAdapter.set(it.data)
                is State.Execute -> it.exe.invoke(showAdapter)
            }
        }

        vm.dispatch(Intent.Set)
    }

    class ShowAdapter : SimpleBaseViewAdapter<RecyclerData, BaseViewHolder>({
        TextView(it, null, R.attr.fastAttrText).apply {
            layoutParams = newMWLP
            padding(horizontal = 16.dp2Px2Int(), vertical = 8.dp2Px2Int())
            clickEffect()
        }
    }) {
        private var move: ((Int) -> Unit)? = null
        override fun onBind(holder: BaseViewHolder, bean: RecyclerData) {
            holder.itemView.to<TextView>().text = "${bean.id} ${bean.data}"
        }

        fun moveAfterAdd(move: ((Int) -> Unit)? = null): ShowAdapter {
            this.move = move
            return this
        }

        override fun add(index: Int, data: Collection<RecyclerData>) {
            super.add(index, data)
            move?.invoke(index)
        }

        override fun add(index: Int, data: RecyclerData) {
            super.add(index, data)
            move?.invoke(index)
        }
    }

    class ConfigRecyclerView : SupportBindConfigDialog<ConfigRecyclerviewBinding>() {

        private val vm by get<RecyclerVM>()

        override fun onCreate(bind: ConfigRecyclerviewBinding) {
            // 由vm来实现来实现set add addMore clear
            bind.rvSet.setOnClickListener { vm.dispatch(Intent.Set) }
            bind.rvAdd.setOnClickListener { vm.dispatch(Intent.Add) }
            bind.rvAddMore.setOnClickListener { vm.dispatch(Intent.AddMore) }
            bind.rvClear.setOnClickListener { vm.dispatch(Intent.Clear) }
        }
    }

    private class TopScroller(private val lm: RecyclerView.LayoutManager) :
        LinearSmoothScroller(AppHelper) {
        override fun getVerticalSnapPreference() = SNAP_TO_START
        override fun setTargetPosition(targetPosition: Int) {
            super.setTargetPosition(targetPosition)
            lm.startSmoothScroll(this)
        }
    }
}