package com.munch.project.one.result

import android.content.Context
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.lib.extend.suspendCancellableCoroutine
import com.munch.lib.extend.toLive
import com.munch.lib.helper.ActivityHelper
import com.munch.lib.result.ResultHelper
import com.munch.lib.result.isGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import android.Manifest.permission as P
import android.content.Intent as I

internal class ResultVM : ViewModel() {

    private val intent = MutableSharedFlow<ResultIntent>()
    private val _data = MutableLiveData<ResultUIState>()
    val data = _data.toLive()
    private val list = mutableListOf<PI>()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            intent.collect { intent ->
                when (intent) {
                    ResultIntent.Refresh -> {
                        updateList()
                        _data.postValue(ResultUIState.Data(list))
                    }
                    is ResultIntent.Selected -> {
                        intent.pi.also { pi -> pi.isSelected = !pi.isSelected }
                        updateList()
                    }
                    ResultIntent.Request -> {
                        val permissions =
                            list.filterIsInstance<PI.Permission>().filter { it.isSelected }
                                .map { it.permission }
                                .toTypedArray()
                        val i = list.filterIsInstance<PI.Intent>()
                            .filter { it.isSelected }
                            .map { it.intent.invoke(ActivityHelper.curr!!) }
                        suspendCancellableCoroutine<Boolean>(Dispatchers.Main) { c ->
                            ResultHelper
                                .with(ActivityHelper.curr as FragmentActivity)
                                .contact()
                                .apply {
                                    permissions.apply { contact(*this) }
                                    i.forEach { contact(it) }
                                }.start {
                                    c.resume(true)
                                }
                        }
                        updateList()
                        _data.postValue(ResultUIState.Data(list))
                    }
                }
            }

        }
    }

    fun dispatch(intent: ResultIntent) {
        viewModelScope.launch {
            this@ResultVM.intent.emit(intent)
        }
    }

    private fun updateList() {
        if (list.isEmpty()) {
            list.addAll(
                arrayListOf(
                    PI.Permission(P.CALL_PHONE),
                    PI.Permission(P.ACCESS_FINE_LOCATION),
                    PI.Permission(P.WRITE_EXTERNAL_STORAGE),
                    PI.Intent.DEVELOP,
                    PI.Intent.SET,
                    PI.Intent.POLICY,
                    PI.Intent.NOTIFICATION
                )
            )
        }
        list.forEach {
            if (it is PI.Permission && it.permission.isGranted()) {
                it.state = PermissionState.Granted
            }
            it.isSelected = false
        }
    }
}

internal sealed class PI {

    var name: String = ""

    // 是否是被选中
    var isSelected = false
    var state: PermissionState = PermissionState.Unknown

    open val desc: String
        get() = ""


    class Permission(val permission: String, val min: Int = 1, val max: Int = Int.MAX_VALUE) :
        PI() {

        init {
            name = permission.replace("android.permission.", "")
        }

        override val desc: String
            get() {
                return if (min == 1 && max == Int.MAX_VALUE) {
                    "$state"
                } else if (max == Int.MAX_VALUE) {
                    "$state, ($min+)"
                } else {
                    "$state, ($min-$max)"
                }
            }

        override fun toString(): String {
            return "Permission($name, $desc, isSelected=$isSelected)"
        }
    }

    class Intent(name: String = "", val intent: (Context) -> I) : PI() {

        init {
            this.name = name
        }

        override val desc: String
            get() = "Intent"

        companion object {

            val DEVELOP = Intent("DEVELOP") { I(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS) }
            val SET = Intent("SET") {
                I(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${it.packageName}")
                )
            }
            val POLICY = Intent("POLICY") { I(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS) }
            val NOTIFICATION =
                Intent("NOTIFICATION") { I(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) }
        }

        override fun toString(): String {
            return "Intent($name, $desc, isSelected=$isSelected)"
        }
    }

    private fun String.isGranted(): Boolean {
        return PermissionChecker.checkSelfPermission(
            ActivityHelper.curr!!, this
        ) == PermissionChecker.PERMISSION_GRANTED
    }

    private fun String.isDeniedForever(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(ActivityHelper.curr!!, this)
    }
}

internal sealed class PermissionState {
    object Granted : PermissionState() {
        override fun toString() = "Granted"
    }

    object Denied : PermissionState() {
        override fun toString() = "Denied"
    }

    object Ask : PermissionState() {
        override fun toString() = "Ask"
    }

    object Unknown : PermissionState() {
        override fun toString() = "Unknown"
    }
}