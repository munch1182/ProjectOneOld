package com.munch.project.one.result

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.AppHelper
import com.munch.lib.extend.*
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.*
import com.munch.lib.helper.ActivityHelper
import com.munch.lib.recyclerview.AdapterFunImp
import com.munch.lib.recyclerview.BaseViewHolder
import com.munch.lib.recyclerview.setOnItemClickListener
import com.munch.lib.result.permissionRequest
import com.munch.lib.task.ThreadHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.munch.lib.recyclerview.BaseRecyclerViewAdapter as BADA

/**
 * Created by munch1182 on 2022/4/21 20:31.
 */
class ResultActivity : BaseFastActivity(),
    ActivityDispatch by (SupportActionBar + SupportConfigDialog({ ResultDialog() })) {

    private val bind by fvLinesRv(
        emptyList(),
        AdapterFunImp.Differ(object : DiffUtil.ItemCallback<Pair<String, String>>() {
            override fun areItemsTheSame(
                oldItem: Pair<String, String>,
                newItem: Pair<String, String>
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: Pair<String, String>,
                newItem: Pair<String, String>
            ): Boolean {
                return oldItem.second == newItem.second
            }

        }, ThreadHelper.mainHandler)
    )
    private val adapter by lazy { bind.adapter }
    private val vm by get<ResultVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bind.init()

        adapter.setOnItemClickListener { _, pos, _ ->
            lifecycleScope.launch(Dispatchers.IO) { vm.get(pos)?.request() }
        }
        adapter.showRefresh()
        vm.pbs().observe(this) { adapter.set(it) }
    }

    override fun onResume() {
        super.onResume()
        vm.refresh()
    }

    class ResultVM : ViewModel() {

        private val all = mutableListOf(
            RBWrapper(true, SB("SET")),
            RBWrapper(true, PB("NET", requestName = Manifest.permission.INTERNET)),
            RBWrapper(true, PB("LOCATION", requestName = Manifest.permission.ACCESS_FINE_LOCATION)),
            RBWrapper(true, PB("CAMERA", requestName = Manifest.permission.CAMERA)),
            RBWrapper(true, PB("CALL", requestName = Manifest.permission.CALL_PHONE)),
        )
        private val pbs = MutableLiveData<List<Pair<String, String>>>(emptyList())
        private var curr = all
        fun pbs() = pbs.toLive()
        fun allList() = all

        fun refresh() {
            viewModelScope.launch(Dispatchers.Default) {
                pbs.postValue(all.filter { it.check }
                    .map { it.rb.name to it.rb.state })
            }
        }

        fun get(pos: Int) = curr.getOrNull(pos)?.rb

        init {
            refresh()
        }
    }

    class RBWrapper(var check: Boolean, val rb: ResultBean)

    class ResultDialog : ConfigDialog(), CompoundButton.OnCheckedChangeListener {

        private val vm by get<ResultVM>()
        private val rv by lazy { RecyclerView(requireContext()) }
        private val adapter by lazy {
            object : BADA<RBWrapper, BaseViewHolder>({
                CheckBox(it)
            }) {
                override fun onBind(
                    holder: BaseViewHolder, position: Int, bean: RBWrapper
                ) {
                    (holder.itemView as CheckBox).apply {
                        text = bean.rb.name
                        isChecked = bean.check
                        tag = position
                        setOnCheckedChangeListener(this@ResultDialog)
                    }

                }
            }
        }

        override fun inflaterView(inflater: LayoutInflater, container: ViewGroup?): View? {
            return super.inflaterView(inflater, container)?.toViewGroup()?.apply {
                showSplit()
                addView(rv, 0)
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            rv.layoutManager = GridLayoutManager(requireContext(), 2)
            rv.adapter = adapter

            adapter.set(vm.allList())
        }

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            val btn = buttonView ?: return
            val i = btn.tag as? Int ?: return
            adapter.get(i)?.check = isChecked
            vm.refresh()
        }
    }

    abstract class ResultBean(
        val name: String,
        val minSdk: Int = 1,
        val maxSdk: Int = Int.MAX_VALUE,
        val requestName: String = name
    ) {

        abstract val state: String

        abstract suspend fun request(): Boolean
    }

    class SB(
        name: String,
        midSdk: Int = 1,
        maxSdk: Int = Int.MAX_VALUE,
        requestName: String = name
    ) : ResultBean(name, midSdk, maxSdk, requestName) {
        override val state: String
            get() = "goto"

        override suspend fun request() = suspendCancellableCoroutine<Boolean> {
            runBlocking(Dispatchers.Main) {
                (ActivityHelper.curr as FragmentActivity).startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${AppHelper.app.packageName}")
                    )
                )
            }
            it.resume(true)
        }

    }

    class PB(
        name: String,
        midSdk: Int = 1,
        maxSdk: Int = Int.MAX_VALUE,
        requestName: String = name
    ) : ResultBean(name, midSdk, maxSdk, requestName) {

        override val state: String
            get() = "${hasPermission()} $minSdk${if (maxSdk == Int.MAX_VALUE) "+" else " ~ $maxSdk"}"

        override suspend fun request() = suspendCancellableCoroutine<Boolean> {
            if (Build.VERSION.SDK_INT in minSdk..maxSdk) {
                runBlocking(Dispatchers.Main) {
                    it.resume(
                        (ActivityHelper.currCreate as FragmentActivity)
                            .permissionRequest(requestName)
                    )
                }
            } else {
                it.resume(true)
            }
        }

        private fun hasPermission(): String {
            return if (isPermissionGranted(requestName)) {
                "Granted"
            } else {
                if (ActivityHelper.currCreate!!.notDeniedForever(requestName)) {
                    "denied"
                } else {
                    "denied forever"
                }
            }
        }

    }

}