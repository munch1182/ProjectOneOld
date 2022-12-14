package com.munch.project.one.base

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.core.view.MenuProvider
import com.munch.lib.android.define.Receive
import com.munch.lib.android.extend.dp2Px2Int
import com.munch.lib.android.extend.padding
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

fun dispatchDef(message: String) = dispatchDef(MessageDialog(message))

class MessageDialog(private val message: String) : SupportBindConfigDialog() {

    override fun onCreate(context: Context) {
        setContent(TextView(context).apply {
            padding(t = 16.dp2Px2Int(), b = 16.dp2Px2Int())
            text = message
        })
    }

}

