package com.munch.project.testsimple

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.munch.lib.test.recyclerview.TestRvActivity
import com.munch.lib.test.recyclerview.TestRvAdapter
import com.munch.lib.test.recyclerview.TestRvItemBean

/**
 * Create by munch1182 on 2020/12/16 17:09.
 */
open class TestFunInFragmentActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_fun_in_fragment)
        supportFragmentManager.beginTransaction()
            .replace(R.id.test_fragment_fragment, TestFunFragment())
            .commit()
    }

    open class TestFunFragment : BaseFragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.activity_rv_test, container, false)
        }

        private var adapter: TestRvAdapter? = null

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val rv = view.findViewById<RecyclerView>(R.id.rv_test_rv)
            val srl = view.findViewById<SwipeRefreshLayout>(R.id.rv_test_srl)
            val items: MutableList<TestRvItemBean>? = getItems()
            val isBtn = true
            if (isBtn) {
                rv.setBackgroundColor(Color.TRANSPARENT)
            }
            adapter = TestRvAdapter(items, isBtn).apply {
                if (isBtn) {
                    clickItemListener {
                        clickItem(it, it.tag as Int)
                    }
                }
            }
            rv.adapter = adapter
            if (!isBtn) {
                rv.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
            rv.layoutManager = LinearLayoutManager(context)
            srl.setOnRefreshListener {
                refresh(srl)
            }
        }

        open fun refresh(srl: SwipeRefreshLayout) {
            srl.postDelayed({
                srl.isRefreshing = false
            }, 500)
        }

        open fun getItems(): MutableList<TestRvItemBean>? {
            return TestRvItemBean.newArray("test1", "test2", "test3", "test4")
        }

        open fun clickItem(view: View, pos: Int) {
            when (pos) {
                0 -> {
                }
                1 -> {
                }
                2 -> {
                }
                3 -> {
                }
            }
        }
    }

}