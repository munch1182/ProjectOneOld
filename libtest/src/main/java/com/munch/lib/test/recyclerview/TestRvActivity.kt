package com.munch.lib.test.recyclerview

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.munch.lib.helper.dp2Px
import com.munch.lib.helper.setMargin
import com.munch.lib.test.R
import com.munch.lib.test.TestBaseTopActivity

/**
 * Create by munch1182 on 2020/12/7 10:44.
 */
open class TestRvActivity : TestBaseTopActivity() {

    companion object {

        fun newBundle(
            title: String = "test",
            items: ArrayList<TestRvItemBean>?,
            hasBack: Boolean = true,
            isBtn: Boolean = false
        ): Bundle {
            return Bundle()
                .apply {
                    putString(KEY_TITLE, title)
                    putBoolean(KEY_IS_BACK, hasBack)
                    putBoolean(KEY_IS_BTN, isBtn)
                    putParcelableArrayList(KEY_ITEMS, items)
                }
        }

        private const val KEY_TITLE = "TITLE"
        private const val KEY_ITEMS = "KEY_VIEW_MODEL"
        private const val KEY_IS_BACK = "KEY_HAS_BACK"
        private const val KEY_IS_BTN = "KEY_IS_BTN"
    }

    private val rv: RecyclerView by lazy { findViewById(R.id.rv_test_rv) }
    protected val topView: ViewGroup by lazy { findViewById(R.id.rv_test_rv_top) }
    private val srl by lazy { findViewById<SwipeRefreshLayout>(R.id.rv_test_srl) }
    protected lateinit var adapter: TestRvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setupIntent()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rv_test)

        val extras = intent.extras

        val items: MutableList<TestRvItemBean>? =
            getItems() ?: extras?.getParcelableArrayList(KEY_ITEMS)
        val isBtn = isBtn()
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
            rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        }
        rv.layoutManager = LinearLayoutManager(this)
        srl.setOnRefreshListener {
            refresh(srl)
        }
    }

    open fun refresh(srl: SwipeRefreshLayout) {
        srl.postDelayed({
            srl.isRefreshing = false
        }, 500)
    }

    /**
     * 因为notShowBack调用机会少，所以采取这种形式
     */
    override fun notShowBack() =
        intent.extras?.getBoolean(KEY_IS_BACK, false)?.not() ?: super.notShowBack()

    open fun isBtn(): Boolean = intent.extras?.getBoolean(KEY_IS_BTN, false) ?: false

    /**
     * 在调用intent之前设置一个新的intent
     * 统一继承与[newBundle]调用
     *
     * 注意，此方法调用在[super#onCreate]之前
     */
    open fun setupIntent() {
    }

    /**
     * item点击事件，只有isBtn才有效，因为非btn点击事件固定是跳转页面
     */
    open fun clickItem(view: View, pos: Int) {}

    /**
     * 继承传入数据
     */
    open fun getItems(): MutableList<TestRvItemBean>? {
        return null
    }

    /**
     * 在页面上添加一个测试按键用于临时测试
     * @see testFun
     */
    @SuppressLint("SetTextI18n")
    private fun addTestView(view: View? = null, index: Int) {
        val addView = (view ?: View.inflate(this, R.layout.item_rv_test, null).apply {
            setBackgroundColor(Color.WHITE)
            findViewById<TextView>(R.id.item_rv_test_tv).text = "test"
            setOnClickListener {
                testFun()
            }
        })
        topView.addView(addView, index)
        addView.setMargin(0, 0, 0, dp2Px(1.0f).toInt())
    }

    open fun addStartView(view: View? = null) = addTestView(view, 0)
    open fun addEndView(view: View? = null) = addTestView(view, -1)

    open fun testFun() {
    }
}