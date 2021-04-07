package com.munch.test.lib.pre.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.munch.lib.fast.base.activity.BaseItemActivity
import com.munch.pre.lib.dialog.DialogManager
import com.munch.pre.lib.dialog.DialogManager.Companion.add
import kotlinx.coroutines.*

/**
 * Create by munch1182 on 2021/4/7 16:42.
 */
class DialogActivity : BaseItemActivity() {

    override fun clickItem(pos: Int) {
        when (pos) {
            0 -> {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        DialogManager.INSTANCE.add {
                            newDialog(it, "d1")
                        }
                        launch {
                            delay(100L)
                            DialogManager.INSTANCE.add {
                                newDialog(it, "d2")
                            }
                        }
                        DialogManager.INSTANCE.add {
                            newDialog(it, "d3")
                        }
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