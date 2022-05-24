package com.munch.project.one.result

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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.AppHelper
import com.munch.lib.extend.get
import com.munch.lib.extend.isPermissionGranted
import com.munch.lib.extend.toLive
import com.munch.lib.extend.toViewGroup
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.ConfigDialog
import com.munch.lib.fast.view.fvLinesRv
import com.munch.lib.fast.view.supportDef
import com.munch.lib.helper.ActivityHelper
import com.munch.lib.recyclerview.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import android.Manifest.permission as p

/**
 * Created by munch1182 on 2022/4/21 20:31.
 */
class ResultActivity : BaseFastActivity(), ActivityDispatch by supportDef({ ResultDialog() }) {

    private val bind by fvLinesRv(emptyList(), differ({ o, n -> o.second == n.second }))
    private val adapter by lazy { bind.adapter }
    private val vm by get<ResultVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bind.init()

        adapter.setOnItemClickListener { _, pos, _ ->
            lifecycleScope.launch(Dispatchers.IO) {
                vm.get(pos)?.request()
            }
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
            RBWrapper(true, PB("NET", requestName = p.INTERNET)),
            RBWrapper(true, PB("LOCATION", requestName = p.ACCESS_FINE_LOCATION)),
            RBWrapper(true, PB("CAMERA", requestName = p.CAMERA)),
            RBWrapper(true, PB("CALL", requestName = p.CALL_PHONE)),
            RBWrapper(true, PB("READ PHONE STATE", requestName = p.READ_PHONE_STATE)),
            RBWrapper(true, PB("BLUETOOTH", requestName = p.BLUETOOTH)),
            RBWrapper(true, PB("BLUETOOTH_ADMIN", requestName = p.BLUETOOTH_ADMIN)),
            RBWrapper(
                Build.VERSION.SDK_INT >= 31,
                PB(
                    "BLUETOOTH_ADVERTISE",
                    minSdk = 31,
                    requestName = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) p.BLUETOOTH_ADVERTISE else "")
                )
            ),
            RBWrapper(
                Build.VERSION.SDK_INT >= 31,
                PB(
                    "BLUETOOTH_SCAN",
                    minSdk = 31,
                    requestName = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) p.BLUETOOTH_SCAN else "")
                )
            ),
            RBWrapper(
                Build.VERSION.SDK_INT >= 31,
                PB(
                    "BLUETOOTH_CONNECT",
                    minSdk = 31,
                    requestName = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) p.BLUETOOTH_CONNECT else "")
                )
            ),
        )
        private val pbs = MutableLiveData<List<Pair<String, String>>>(emptyList())
        fun pbs() = pbs.toLive()
        fun allList() = all

        fun refresh() {
            viewModelScope.launch(Dispatchers.Default) {
                pbs.postValue(all.filter { it.check }
                    .map { it.rb.name to it.rb.state })
            }
        }

        fun get(pos: Int) = all.filter { it.check }.getOrNull(pos)?.rb

        init {
            refresh()
        }
    }

    class RBWrapper(var check: Boolean, val rb: ResultBean)

    class ResultDialog : ConfigDialog(), CompoundButton.OnCheckedChangeListener {

        private val vm by get<ResultVM>()
        private val rv by lazy { RecyclerView(requireContext()) }
        private val adapter by lazy {
            object : RVAdapter<RBWrapper>({
                CheckBox(it, null, com.munch.lib.fast.R.attr.fastAttrCheck)
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
        val minSdk: Int,
        val maxSdk: Int,
        val requestName: String = name
    ) {

        abstract val state: String

        abstract suspend fun request(): Boolean
    }

    class SB(
        name: String,
        minSdk: Int = 1,
        maxSdk: Int = Int.MAX_VALUE,
        requestName: String = name
    ) : ResultBean(name, minSdk, maxSdk, requestName) {
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
        minSdk: Int = 1,
        maxSdk: Int = Int.MAX_VALUE,
        requestName: String = name
    ) : ResultBean(name, minSdk, maxSdk, requestName) {

        override val state: String
            get() = "$minSdk${if (maxSdk == Int.MAX_VALUE) "+" else " ~ $maxSdk"} ${hasPermission()}"

        override suspend fun request() = suspendCancellableCoroutine<Boolean> {
            if (Build.VERSION.SDK_INT in minSdk..maxSdk) {
                runBlocking(Dispatchers.Main) {
                    it.resume(
                        /*(ActivityHelper.currCreate as FragmentActivity)
                            .permissionRequest(requestName)*/
                        false
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
                "denied"
            }
        }

    }

}