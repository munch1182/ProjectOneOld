package com.munch.project.test.view

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import com.munch.lib.extend.recyclerview.BaseSimpleBindAdapter
import com.munch.lib.extend.recyclerview.FirstScrollHelper
import com.munch.lib.helper.ThreadHelper
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.test.R
import com.munch.project.test.databinding.TestLayoutItemHeaderRecyclerViewContentBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * Create by munch1182 on 2020/12/24 17:24.
 */
class TestHeaderRecyclerViewActivity : TestBaseTopActivity() {

    private var teardrop: TeardropAngleView? = null
    private val letterNavigation: LetterNavigationBarView by lazy { findViewById(R.id.view_letter_navigation) }
    private val rv: RecyclerView by lazy { findViewById(R.id.view_letter_rv) }
    private val container: ConstraintLayout by lazy { findViewById(R.id.view_letter_container) }
    private val headerItemDecoration by lazy { HeaderItemDecoration() }
    private val firstScrollListener by lazy { FirstScrollHelper() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity_test_header_recycler_view)

        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(applicationContext)))

        rv.layoutManager = LinearLayoutManager(this)
        val iconAdapter =
            BaseSimpleBindAdapter<IconBean, TestLayoutItemHeaderRecyclerViewContentBinding>(
                R.layout.test_layout_item_header_recycler_view_content
            ) { holder, data, _ ->
                holder.binding.itemIconName.text = data.name
            }
        rv.adapter = iconAdapter
        handleItems(getItems()) { items, letters, map ->
            rv.post {
                letterNavigation.setLetters(letters)
                letterNavigation.select(letters[0])
                rv.removeItemDecoration(headerItemDecoration)
                @Suppress("UNCHECKED_CAST")
                headerItemDecoration.resetData(items as ArrayList<HeaderItemDecoration.IsHeader>)
                rv.addItemDecoration(headerItemDecoration)

                iconAdapter.setData(items)

                handleLetterNavigation(map)
            }
        }
        firstScrollListener.bind(rv)

    }

    private fun handleLetterNavigation(map: HashMap<String, Int>) {
        letterNavigation.handleListener = { letter, rect ->
            val height = 240
            var marginBottom = 0
            var marginTop = rect.top + rect.height() / 2 - height / 2
            if (marginTop < 0) {
                marginBottom = marginTop.absoluteValue
                marginTop = 0
            }
            if (teardrop == null) {
                teardrop = TeardropAngleView(this).apply {
                    setProperty {
                        text = letter
                        angle = 0
                        bgColor = ContextCompat.getColor(
                            this@TestHeaderRecyclerViewActivity,
                            R.color.colorPrimary
                        )
                    }
                }
                container.addView(teardrop, ConstraintLayout.LayoutParams(height, height).apply {
                    this.endToStart = R.id.view_letter_navigation
                    this.topToTop = R.id.view_letter_navigation
                    this.bottomToBottom = R.id.view_letter_navigation
                    verticalBias = 0f
                    setMargins(0, marginTop, 0, marginBottom)
                })
            } else {
                teardrop?.run {
                    visibility = View.VISIBLE
                    layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                        setMargins(0, marginTop, 0, marginBottom)
                    }
                    setProperty {
                        text = letter
                    }
                }
            }
            /*letterNavigation.select(letter)*/
            val i = map[letter]
            if (i != null) {
                /*rv.smoothScrollToPosition(i)*/
                firstScrollListener.smoothScrollToPosThenStopFirst(i)
                /*(rv.layoutManager as? LinearLayoutManager?)?.scrollToPositionWithOffset(i, 0)*/
            }
            true
        }

        letterNavigation.selectEndListener = { _, _ ->
            teardrop?.postDelayed({
                teardrop?.visibility = View.GONE
            }, 300L)
        }

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private val manager = rv.layoutManager as LinearLayoutManager
            private val list = arrayListOf<String>()
            private var firstLast = -1
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val first = manager.findFirstVisibleItemPosition()
                if (firstLast == first) {
                    return
                }
                firstLast = first
                val last = manager.findLastVisibleItemPosition()
                list.clear()
                for (i in first..last) {
                    if (map.containsValue(i)) {
                        list.add(map.keys.toList()[map.values.indexOf(i)])
                    }
                }
                letterNavigation.select(*(list.toArray(Array(0) { "" })))
            }
        })
    }

    private fun handleItems(
        items: ArrayList<IconBean>,
        func: (items: ArrayList<IconBean>, letters: ArrayList<String>, map: HashMap<String, Int>) -> Unit
    ) {
        ThreadHelper.getExecutor().execute {
            items.sort()
            val letters = arrayListOf<String>()
            val map = HashMap<String, Int>()
            items.forEachIndexed { index, bean ->
                if (bean.letter != null) {
                    if (!letters.contains(bean.letter.toString())) {
                        bean.setHeader()
                        letters.add(bean.letter.toString())
                        map[bean.letter.toString()] = index
                    }
                }
            }
            func.invoke(items, letters, map)
        }
    }

    private fun getItems(): ArrayList<IconBean> {
        val arrayList = ArrayList<IconBean>()
        for (i in 0..100) {
            arrayList.add(IconBean.newInstance())
        }
        return arrayList
    }

    data class IconBean(
        var isHeader: Boolean = false,
        val letter: Char? = null,
        var name: String? = null,
        //保留字符的全部拼音，用作同音排序
        var pinyin: String? = null,
        var icon: String? = null
    ) : Comparable<IconBean>, HeaderItemDecoration.IsHeader {

        fun setHeader(isHeader: Boolean = true): IconBean {
            this.isHeader = isHeader
            return this
        }

        companion object {

            fun newContent(name: String): IconBean {
                val c = name.toCharArray()[0]
                val py = Pinyin.toPinyin(c)
                return if (py.isEmpty() || c.isDigit()) {
                    IconBean(false, '#', name)
                } else {
                    IconBean(false, py.toCharArray()[0].toUpperCase(), name, py)
                }
            }

            private val chars = "赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张123absde".split("")

            fun newInstance(): IconBean {
                return newContent("${chars[Random.nextInt(32)]}${Random.nextInt(1234)}")
            }
        }

        override fun compareTo(other: IconBean): Int {
            if (other.pinyin == null && pinyin == null) {
                return 0
            }
            return this.pinyin?.toUpperCase(Locale.ROOT)
                ?.compareTo(
                    other.pinyin?.toUpperCase(Locale.ROOT) ?: return -1
                ) ?: 1
        }

        override fun isHeaderItem(): Boolean = isHeader

        override fun headerStr(): String = letter?.toString() ?: ""

    }
}