package com.munch.project.one.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.app.AppHelper
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.recyclerview.SimpleDiffAdapter
import com.munch.lib.fast.recyclerview.setOnItemClickListener
import com.munch.lib.fast.recyclerview.setOnViewClickListener
import com.munch.lib.helper.PhoneHelper
import com.munch.lib.result.ResultHelper
import com.munch.project.one.R
import com.munch.project.one.databinding.ActivityPermissionsBinding
import com.munch.project.one.databinding.ItemPermissionBinding

/**
 * Create by munch1182 on 2021/10/13 14:29.
 */
class PermissionActivity : BaseBigTextTitleActivity() {

    companion object {

        @SuppressLint("InlinedApi")
        val PERMISSIONS = arrayListOf(
            PB(Manifest.permission.CALL_PHONE, 1, "普通权限"),
            PB(
                Manifest.permission.WRITE_EXTERNAL_STORAGE, 1, Build.VERSION_CODES.Q,
                "Android10及以上此权限无效"
            ),
            PB(Manifest.permission.ACCESS_FINE_LOCATION, 1, "此权限有本次运行允许选项"),
            PB(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION, Build.VERSION_CODES.Q,
                "此权限需要先申请定位权限；在Android10中，可以和定位权限一起申请；在Android11中，只能获得定位权限之后，才能申请此权限"
            ),
            PB(Manifest.permission.MANAGE_EXTERNAL_STORAGE, Build.VERSION_CODES.R),
            PB(Manifest.permission.CAMERA, 1, "此权限有本次运行允许选项"),
            PB(Manifest.permission.RECORD_AUDIO, 1, "此权限有本次运行允许选项"),
        )
    }

    private val bind by bind<ActivityPermissionsBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.permissionVersion.text = String.format(
            "android%s, version:%s",
            PhoneHelper.getSDKRelease().toString(),
            Build.VERSION.SDK_INT.toString()
        )

        val pa = SimpleDiffAdapter<PermissionBean, ItemPermissionBinding>(
            R.layout.item_permission, PermissionBeanDiffUtil()
        ) { _, db, bean -> db.permission = bean }
        bind.permissionRv.apply {
            layoutManager = LinearLayoutManager(this@PermissionActivity)
            adapter = pa
        }
        pa.setOnItemClickListener { _, pos, _ ->
            val pb = pa.data[pos] ?: return@setOnItemClickListener
            AlertDialog.Builder(this)
                .setTitle(pb.name.replace("android.permission.", ""))
                .setMessage(pb.other)
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                .show()
        }
        pa.setOnViewClickListener({ _, pos, _ ->
            val pb = pa.data[pos] ?: return@setOnViewClickListener
            val name = pb.name
            val start = pb.isGranted
            if (start) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                ResultHelper.init(this)
                    .with(intent)
                    .start {
                        if (it && pb.isGranted != start) {
                            pa.notifyItemChanged(pos)
                        }
                    }
            } else {
                ResultHelper.init(this)
                    .with(name)
                    .request {
                        if (it && pb.isGranted != start) {
                            pa.notifyItemChanged(pos)
                        }
                    }
            }
        }, R.id.permission_request)

        pa.set(PERMISSIONS.toMutableList())
    }

    class PermissionBeanDiffUtil : DiffUtil.ItemCallback<PermissionBean>() {
        override fun areItemsTheSame(
            oldItem: PermissionBean,
            newItem: PermissionBean
        ): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: PermissionBean,
            newItem: PermissionBean
        ): Boolean {
            return oldItem.isGranted == newItem.isGranted
        }
    }
}

typealias PB = PermissionBean

data class PermissionBean(
    val name: String,
    val version: Int,
    val needJump: Boolean,
    val maxVersion: Int = Int.MAX_VALUE,
    var other: String = ""
) {

    companion object {

        @JvmStatic
        @BindingAdapter("bind_permission")
        fun bind(btn: Button, bean: PermissionBean) {
            if (!bean.isSupport) {
                btn.text = "不可用"
                btn.isEnabled = false
                return
            }
            btn.text = if (bean.isGranted) "已获取" else "申请"
        }
    }

    val isGranted: Boolean
        get() = ActivityCompat.checkSelfPermission(
            AppHelper.app, name
        ) == PackageManager.PERMISSION_GRANTED
    val isSupport: Boolean
        get() = Build.VERSION.SDK_INT in version..maxVersion

    val versionStr: String
        get() = "Version: $version${if (maxVersion != Int.MAX_VALUE) "-$maxVersion" else "+"} "
    val needJumpStr: String
        get() = "Jump: $needJump"

    constructor(
        name: String,
        version: Int,
        other: String = ""
    ) : this(name, version, false, Int.MAX_VALUE, other)

    constructor(
        name: String,
        version: Int,
        maxVersion: Int,
        other: String = ""
    ) : this(
        name, version, false, maxVersion - 1, other
    )
}