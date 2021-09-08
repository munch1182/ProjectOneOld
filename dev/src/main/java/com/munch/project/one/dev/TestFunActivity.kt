package com.munch.project.one.dev

import com.munch.lib.fast.base.BaseBtnWithNoticeActivity
import com.munch.lib.fast.databinding.ItemSimpleBtnWithNoticeBinding
import com.munch.lib.log.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/9/1 13:56.
 */
class TestFunActivity : BaseBtnWithNoticeActivity() {

    override fun getData(): MutableList<String?> {
        return mutableListOf("FUN1", "FUN2", "FUN3", "FUN4", "FUN5")
    }

    override fun onClick(pos: Int, bind: ItemSimpleBtnWithNoticeBinding) {
        super.onClick(pos, bind)
        when (pos) {
            0 -> {
                val c = Channel<Int>(capacity = 10)
                val list = mutableListOf<Int>()
                GlobalScope.launch(Dispatchers.IO) {
                    var stop = false
                    for (i in c) {
                        list.add(i)
                        if (!stop) {
                            stop = true
                            GlobalScope.launch(Dispatchers.IO) {
                                log(Thread.currentThread().name)
                                while (list.size != 0) {
                                    val handleList = list.toMutableList()
                                    log("handle: ${handleList.joinToString()}")
                                    delay(5L)
                                    list.removeAll(handleList)
                                }
                                stop = false
                            }
                        }

                    }
                }
                GlobalScope.launch(Dispatchers.IO) {
                    repeat(130) {
                        c.send(it)
                        log("send $it")
                        delay(3L)
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
}