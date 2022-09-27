package com.munch.project.one.recyclerview

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.android.AppHelper
import com.munch.lib.android.dialog.IDialog
import com.munch.lib.android.extend.*
import com.munch.lib.android.recyclerview.*
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
        rv.adapter = changeAdapterType(rv)


        vm.state.observe(this) {
            when (it) {
                is State.Operate -> it.op.invoke(rv.adapter!!.to())
                State.NextType -> {
                    vm.dispatch(Intent.Clear)
                    rv.adapter = changeAdapterType(rv)
                    vm.dispatch(Intent.NewData)
                }
            }
        }

        vm.dispatch(Intent.NewData)
    }

    private fun changeAdapterType(rv: RecyclerView): ShowAdapter {
        val lm = rv.layoutManager!!.to<LinearLayoutManager>()
        val scroller = TopScroller(lm) // 完全移动到页面上方

        val showAdapter = when (rv.adapter) {
            null -> NormalAdapter(scroller)
            is NormalAdapter -> DiffAdapter(scroller)
            is DiffAdapter -> NormalAdapter(scroller)
            else -> NormalAdapter(scroller)
        }
        showAdapter.setOnItemClick { vm.dispatch(Intent.Update(it.pos)) }
            .setOnItemLongClick { vm.dispatch(Intent.Remove(it.pos)) } // 这里统一交给VM处理
        toast("curr: ${showAdapter::class.java.simpleName}")
        return showAdapter
    }

    abstract class ShowAdapter(
        private val scroller: LinearSmoothScroller,
        dataHelper: AdapterFunHelper<RecyclerData>
    ) : SimpleBaseViewAdapter<RecyclerData>({
        TextView(it, null, R.attr.fastAttrText).apply {
            layoutParams = newMWLP
            padding(horizontal = 16.dp2Px2Int(), vertical = 8.dp2Px2Int())
            clickEffect()
        }
    }, dataHelper), RecyclerAdapterDataFun {

        override fun onBind(holder: SimpleVH, bean: RecyclerData) {
            holder.itemView.to<TextView>().text = if (holder.pos == bean.id) {
                "${holder.pos}: ${bean.data}"
            } else {
                val it = "${holder.pos}(${bean.id}): ${bean.data}"
                val first = it.indexOfFirst { c -> c == '(' }
                val end = it.indexOfFirst { c -> c == ')' }
                it.color(Color.RED, first, end)
            }
        }

        override fun remove(index: Int) {
            super<SimpleBaseViewAdapter>.remove(index)
            // 因为UI绑定了holder的pos, 所以需要强制更新, 实际使用时不需要, 未强制刷新点击时获取的pos也是正确的
            notifyItemRangeChanged(index, itemCount)
        }

        override fun moveTo(index: Int) {
            scroller.targetPosition = index
        }
    }

    private class NormalAdapter(scroller: LinearSmoothScroller) :
        ShowAdapter(scroller, SimpleAdapterFun())

    private class DiffAdapter(scroller: LinearSmoothScroller) :
        ShowAdapter(scroller, DifferAdapterFun(differ({ data.hashCode() })))

    private class ConfigRecyclerView : SupportBindConfigDialog() {

        private val bind by bind<ConfigRecyclerviewBinding>()
        private val vm by get<RecyclerVM>()
        private var dialog: IDialog? = null

        override fun onCreateDialog(activity: AppCompatActivity): IDialog {
            return super.onCreateDialog(activity).apply { this@ConfigRecyclerView.dialog = this }
        }

        override fun onCreate(context: Context) {
            bind.rvType.setOnClickListener { vm.dispatch(Intent.NextType) }
            bind.rvSet.setOnClickListener { vm.dispatch(Intent.NewData) }
            bind.rvAddOne.setOnClickListener { vm.dispatch(Intent.AddOne) }
            bind.rvAddMore.setOnClickListener { vm.dispatch(Intent.AddList) }
            bind.rvClear.setOnClickListener { vm.dispatch(Intent.Clear) }
            bind.rvRemoveRange.setOnClickListener { vm.dispatch(Intent.RemoveRange) }
            bind.rvAddAdd.setOnClickListener {
                vm.dispatch(Intent.AddAdd)
                this.dialog?.dismiss()
            }
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