package com.munch.project.one.result

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.extend.LinearLineItemDecoration
import com.munch.lib.extend.bind
import com.munch.lib.extend.get
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.supportDef
import com.munch.lib.recyclerview.*
import com.munch.project.one.databinding.ActivityResultBinding
import com.munch.project.one.databinding.ItemResultBinding

/**
 * Created by munch1182 on 2022/4/21 20:31.
 */
class ResultActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val bind by bind<ActivityResultBinding>()
    private val vm by get<ResultVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resultAdapter = ResultAdapter { pi, _ ->
            vm.dispatch(ResultIntent.Selected(pi))
        }
        val adapter = AdapterHelper(resultAdapter)
        bind.resultRv.apply {
            val lm = LinearLayoutManager(ctx)
            layoutManager = lm
            adapter.bind(this)
            addItemDecoration(LinearLineItemDecoration(lm))
        }
        adapter.setOnItemClickListener { _, pos, _ ->
            if (resultAdapter.isSelectedMode) {
                resultAdapter.get(pos)?.let {
                    it.isSelected = !it.isSelected
                    resultAdapter.update(pos, true)
                }
            } else {
                resultAdapter.get(pos)?.let {
                    it.isSelected = true
                    vm.dispatch(ResultIntent.Request)
                }
            }
        }
        adapter.setOnItemLongClickListener { _, _, _ -> showBtn(resultAdapter.toggle()) }
        adapter.showRefresh()
        bind.resultSure.setOnClickListener {
            vm.dispatch(ResultIntent.Request)
            showBtn(resultAdapter.setSelectedMode(false))
        }

        vm.data.observe(this) {
            when (it) {
                is ResultUIState.Data -> {
                    adapter.set(it.data)
                    showBtn(resultAdapter.setSelectedMode(false))
                }
            }
        }
    }

    private fun showBtn(b: Boolean): Boolean {
        bind.resultContainer.visibility = if (b) View.VISIBLE else View.GONE
        bind.resultSure.visibility = if (b) View.VISIBLE else View.GONE
        bind.resultRv.setPadding(0, 0, 0, if (b) bind.resultContainer.height else 0)
        return true
    }

    override fun onResume() {
        super.onResume()
        vm.dispatch(ResultIntent.Refresh)
    }

    private class ResultAdapter(var onItemCheck: ((PI, Boolean) -> Unit)? = null) :
        BindRVAdapter<PI, ItemResultBinding>() {

        var isSelectedMode = false
            private set

        override fun onBind(
            holder: BaseBindViewHolder<ItemResultBinding>,
            bean: PI
        ) {
            holder.bind.apply {
                resultName.text = bean.name
                resultDesc.text = bean.desc
            }
        }

        override fun onBind(
            holder: BaseBindViewHolder<ItemResultBinding>,
            bean: PI,
            payloads: MutableList<Any>
        ) {
            super.onBind(holder, bean, payloads)
            holder.bind.resultCb.apply {
                isChecked = bean.isSelected
                visibility = if (payloads.isNotEmpty()) View.VISIBLE else View.INVISIBLE
                setOnCheckedChangeListener { _, isChecked ->
                    onItemCheck?.invoke(bean, isChecked)
                }
            }
        }

        fun toggle() = setSelectedMode(!isSelectedMode)

        fun setSelectedMode(isSelectedMode: Boolean): Boolean {
            update(0, itemSize, if (isSelectedMode) true else null)
            this.isSelectedMode = isSelectedMode
            return isSelectedMode
        }

    }
}

