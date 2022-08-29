package com.munch.project.one.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import com.munch.lib.OnCancel
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.extend.bind
import com.munch.lib.extend.lazy
import com.munch.lib.fast.base.BaseFastActivity
import com.munch.lib.fast.helper.ViewColorHelper
import com.munch.lib.fast.view.ActivityDispatch
import com.munch.lib.fast.view.supportDef
import com.munch.lib.notice.Notice
import com.munch.lib.notice.OnSelect
import com.munch.lib.notice.OnSelectOk
import com.munch.lib.result.ExplainContactNotice
import com.munch.lib.result.contact
import com.munch.project.one.R
import com.munch.project.one.databinding.ActivityBluetoothBinding

/**
 * Created by munch1182 on 2022/5/18 21:20.
 */
@SuppressLint("SetTextI18n")
class BluetoothActivity : BaseFastActivity(), ActivityDispatch by supportDef() {

    private val bind by bind<ActivityBluetoothBinding>()
    private val barText by lazy { TextView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addRight(barText)

        if (!BluetoothHelper.instance.isSupportBle) {
            bind.typeLe.visibility = View.GONE
            bind.btType.check(R.id.type_classic)
        }

        ViewColorHelper.onUpdate {
            bind.typeLe.setTextColor(Color.BLACK)
            bind.typeClassic.setTextColor(Color.BLACK)
        }
        bind.btn.setOnClickListener {
            checkOrRequest {
                BluetoothHelper.instance.startScan()
            }
        }

    }

    private fun checkOrRequest(grant: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contact(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            contact(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }.contact(
            { BluetoothHelper.instance.isEnable },
            { Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE) }
        ).contact(
            {
                val lm = it.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            },
            { Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS) }
        ).explain { GPSExplain(it) }
            .start {
                if (it) grant.invoke()
            }
    }

    private class GPSExplain(private val context: Context) : ExplainContactNotice {

        private val db = AlertDialog.Builder(context)
        private var dialog: AlertDialog? = null
            get() = field ?: db.create().also { field = it }

        override fun onIntentExplain(intent: Intent): Boolean {
            when (intent.action) {
                Settings.ACTION_LOCATION_SOURCE_SETTINGS -> db.setMessage("搜寻蓝牙设备需要GPS服务")
                BluetoothAdapter.ACTION_REQUEST_ENABLE -> db.setMessage("需要开启蓝牙")
                else -> return false
            }
            return true
        }

        override fun onPermissionExplain(
            isBeforeRequest: Boolean,
            permissions: Map<String, Boolean>
        ): Boolean {
            return false
        }

        override fun show() {
            dialog?.show()
        }

        override fun cancel() {
            dialog?.cancel()
            dialog = null
        }

        override fun addOnCancel(onCancel: OnCancel?): GPSExplain {
            dialog?.setOnCancelListener {
                onCancel?.invoke()
                dialog = null
            }
            return this
        }

        override fun addOnSelect(chose: OnSelect): GPSExplain {
            return this
        }

        override fun addOnSelectCancel(cancel: OnSelectOk): Notice {
            dialog?.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                context.getString(android.R.string.cancel)
            ) { d, _ ->
                d.cancel()
                cancel.invoke()
            }
            return this
        }

        override fun addOnSelectOk(ok: OnSelectOk): Notice {
            dialog?.setButton(
                DialogInterface.BUTTON_POSITIVE,
                context.getString(android.R.string.ok)
            ) { d, _ ->
                d.cancel()
                ok.invoke()
            }
            return this
        }

        override val isShowing: Boolean
            get() = dialog?.isShowing ?: false
    }
}