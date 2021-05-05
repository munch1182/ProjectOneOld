package com.munch.test.project.one.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.munch.pre.lib.dialog.DialogManager
import com.munch.pre.lib.dialog.DialogManager.Companion.add
import com.munch.test.project.one.base.BaseItemActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/4/7 16:42.
 */
class DialogActivity : BaseItemActivity() {

    override fun clickItem(pos: Int) {
        when (pos) {
            0 -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    DialogManager.INSTANCE.add {
                        newDialog(it, "d1")
                    }
                    launch(Dispatchers.IO) {
                        delay(1000L)
                        DialogManager.INSTANCE.add {
                            AlertDialog.Builder(it)
                                .setTitle("d3")
                                .setMessage("d3 content")
                                .setNegativeButton("取消") { _, _ ->
                                    GlobalScope.launch(Dispatchers.IO) {
                                        delay(500L)
                                        DialogManager.INSTANCE.add { con ->
                                            newDialog(con, "d5")
                                        }
                                    }
                                }
                                .create()
                        }
                    }
                    DialogManager.INSTANCE.add {
                        AlertDialog.Builder(it)
                            .setTitle("d2")
                            .setMessage("d2 content")
                            .setNegativeButton("取消") { _, _ -> onBackPressed() }
                            .create()
                    }
                    launch(Dispatchers.Main) {
                        DialogManager.INSTANCE.add {
                            newDialog(it, "d4")
                        }
                    }
                }
            }
            1 -> {
            }
            2 -> {
            }
            3 -> {
            }
            4 -> {
            }
            else -> {
            }
        }
    }

    private fun newDialog(it: Context, title: String) = AlertDialog.Builder(it)
        .setTitle(title)
        .setMessage("$title content")
        .setNegativeButton("取消") { _, _ -> }
        .create()

    override fun getItem(): MutableList<String> {
        return mutableListOf("d1", "d2", "d3", "d4")
    }
}