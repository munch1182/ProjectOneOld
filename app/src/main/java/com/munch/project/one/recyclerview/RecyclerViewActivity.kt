package com.munch.project.one.recyclerview

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.android.AppHelper
import com.munch.lib.android.dialog.IDialog
import com.munch.lib.android.extend.LinearLineItemDecoration
import com.munch.lib.android.extend.get
import com.munch.lib.android.extend.to
import com.munch.lib.android.extend.toast
import com.munch.lib.android.recyclerview.BaseRecyclerViewAdapter
import com.munch.lib.android.recyclerview.pos
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
                    vm.dispatch(Intent.NewData(getType(rv)))
                }
            }
        }

        vm.dispatch(Intent.NewData(getType(rv)))
    }

    private fun getType(rv: RecyclerView): Int {
        return when (rv.adapter) {
            null -> Intent.TYPE_NORMAL
            is NormalAdapter -> Intent.TYPE_NORMAL
            is MultiAdapter -> Intent.TYPE_MULTI
            else -> Intent.TYPE_NORMAL
        }
    }

    private fun changeAdapterType(rv: RecyclerView): RecyclerView.Adapter<*> {
        val lm = rv.layoutManager!!.to<LinearLayoutManager>()
        val scroller = TopScroller(lm) // 完全移动到页面上方

        val showAdapter: BaseRecyclerViewAdapter<RecyclerData, RecyclerView.ViewHolder> =
            when (rv.adapter) {
                null -> NormalAdapter(scroller).to()
                is NormalAdapter -> DiffAdapter(scroller).to()
                is DiffAdapter -> MultiAdapter(scroller).to()
                is MultiAdapter -> NormalAdapter(scroller).to()
                else -> MultiAdapter(scroller).to()
            }
        showAdapter.setOnItemClick { vm.dispatch(Intent.Update(it.pos)) }
            .setOnItemLongClick { vm.dispatch(Intent.Remove(it.pos)) } // 这里统一交给VM处理
        toast("curr: ${showAdapter::class.java.simpleName}")
        return showAdapter
    }

    private class ConfigRecyclerView : SupportBindConfigDialog() {

        private val bind by bind<ConfigRecyclerviewBinding>()
        private val vm by get<RecyclerVM>()
        private var dialog: IDialog? = null

        override fun onCreateDialog(activity: AppCompatActivity): IDialog {
            return super.onCreateDialog(activity).apply { this@ConfigRecyclerView.dialog = this }
        }

        override fun onCreate(context: Context) {
            bind.rvType.setOnClickListener { vm.dispatch(Intent.NextType) }
            bind.rvSet.setOnClickListener { vm.dispatch(Intent.NewData(Intent.TYPE_SAME)) }
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