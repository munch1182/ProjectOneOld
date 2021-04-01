package com.munch.test.lib.pre.dag

import android.os.Bundle
import com.munch.lib.fast.base.BaseItemWithNoticeActivity
import com.munch.pre.lib.dag.Dag
import com.munch.pre.lib.extend.log
import com.munch.pre.lib.extend.obOnResume
import com.munch.pre.lib.log.LogLog

/**
 * Create by munch1182 on 2021/4/1 15:19.
 */
class DagActivity : BaseItemWithNoticeActivity() {
    override fun clickItem(pos: Int) {
        when (pos) {
            0 -> log(
                Dag<String>()
                    .addEdge(Dag.Edge(Dag.Point("1"), Dag.Point("4")))
                    .addEdge(Dag.Edge(Dag.Point("1"), Dag.Point("3", 2)))
                    .addEdge(Dag.Edge(Dag.Point("2"), Dag.Point("3")))
                    .addEdge(Dag.Edge(Dag.Point("2"), Dag.Point("5", 3)))
                    .addEdge(Dag.Edge(Dag.Point("4"), Dag.Point("6")))
                    .addEdge(Dag.Edge(Dag.Point("3"), Dag.Point("6")))
                    .addEdge(Dag.Edge(Dag.Point("3"), Dag.Point("6")))
                    .addEdge(Dag.Edge(Dag.Point("3"), Dag.Point("6")))
                    .addEdge(Dag.Edge(Dag.Point("3"), Dag.Point("6")))
                    .addEdge(Dag.Edge(Dag.Point("5"), Dag.Point("7")))
                    .addEdge(Dag.Edge(Dag.Point("6"), Dag.Point("9")))
                    .addEdge(Dag.Edge(Dag.Point("6"), Dag.Point("8", 2)))
                    .addEdge(Dag.Edge(Dag.Point("7"), Dag.Point("8")))
                    .addEdge(Dag.Edge(Dag.Point("7"), Dag.Point("10")))
                    .addEdge(Dag.Edge(Dag.Point("3"), Dag.Point("8")))
                    .generaDag()
            )
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

    override fun getItem(): MutableList<String> {
        return mutableListOf("test dag", "test dialog")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        obOnResume({ LogLog.setListener { msg, _ -> notice(msg) } }, { LogLog.setListener() })
    }
}