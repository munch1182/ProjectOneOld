package com.munch.project.one

import android.content.Intent
import android.os.Bundle
import com.munch.lib.android.extend.catch
import com.munch.lib.android.extend.ctx
import com.munch.lib.fast.view.DataHelper
import com.munch.lib.fast.view.dispatch.ActivityDispatch
import com.munch.lib.fast.view.dispatch.SupportActionBar
import com.munch.lib.fast.view.fastview.fvRvTv
import com.munch.plugin.annotation.Measure
import com.munch.project.one.base.BaseActivity
import com.munch.project.one.bluetooth.BluetoothActivity
import com.munch.project.one.dialog.DialogActivity
import com.munch.project.one.file.FileActivity
import com.munch.project.one.notify.NotifyActivity
import com.munch.project.one.recyclerview.RecyclerViewActivity
import com.munch.project.one.simple.PhoneInfoActivity
import com.munch.project.one.simple.StatusBarActivity

@Measure
class MainActivity : BaseActivity(), ActivityDispatch by SupportActionBar(false) {

    private val bind by fvRvTv(
        TestActivity::class,
        BluetoothActivity::class,
        NotifyActivity::class,
        DialogActivity::class,
        StatusBarActivity::class,
        FileActivity::class,
        PhoneInfoActivity::class,
        RecyclerViewActivity::class,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.init()
        catch { DataHelper.firstPage?.let { startActivity(Intent(ctx, it)) } }
    }
}

