package com.munch.project.one.base

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.munch.lib.android.define.Receive
import com.munch.lib.fast.view.dispatch.*

open class BaseActivity : DispatcherActivity() {

    protected open var menu: Menu? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    protected open fun addItem(vararg name: String, onSelect: Receive<MenuItem>? = null) {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                name.forEach { menu.add(it).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM) }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                onSelect?.invoke(menuItem)
                return true
            }
        }, this)
    }
}

fun dispatchDef(dialog: IConfigDialog = SupportConfigDialog()): ActivityDispatch {
    return SupportActionBar() + dialog
}

