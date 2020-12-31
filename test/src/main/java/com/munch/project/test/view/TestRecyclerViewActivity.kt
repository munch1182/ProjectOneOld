package com.munch.project.test.view

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import com.munch.lib.helper.ThreadHelper
import com.munch.lib.log
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.test.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.math.absoluteValue
import kotlin.math.log
import kotlin.random.Random

/**
 * Create by munch1182 on 2020/12/24 17:24.
 */
class TestRecyclerViewActivity : TestBaseTopActivity() {

    private val letterNavigation: LetterNavigationBarView by lazy { findViewById(R.id.view_letter_navigation) }
    private val rv: RecyclerView by lazy { findViewById(R.id.view_letter_rv) }
    private val rvLetter: RecyclerView by lazy { findViewById(R.id.view_letter_rv_letter) }
    private val container: ConstraintLayout by lazy { findViewById(R.id.view_letter_container) }
    private var teardrop: TeardropAngleView? = null

    companion object {
        private var count = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_recycler_view)

        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(applicationContext)))

        rv.layoutManager = GridLayoutManager(this, count)
        val iconAdapter = IconAdapter()
        rv.adapter = iconAdapter

        val letterAdapter = LetterAdapter()
        rvLetter.layoutManager = LinearLayoutManager(this)
        rvLetter.adapter = letterAdapter

        val map = linkedMapOf<String, Int>()

        handleItems(getItems()) { letters, beans ->
            val lettersOnly = arrayListOf<String>()
            letters.forEachIndexed { index, s ->
                if (s.isNotEmpty()) {
                    lettersOnly.add(s)
                    map[s] = index
                }
            }
            container.post {
                iconAdapter.setData(beans)
                letterAdapter.setData(letters)

                letterNavigation.setLetters(lettersOnly)
                letterNavigation.select(lettersOnly[0])

                handleLetterNavigation(map)
            }
        }

        syncScroll(map)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun syncScroll(map: LinkedHashMap<String, Int>) {
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (RecyclerView.SCROLL_STATE_IDLE != recyclerView.scrollState) {
                    rvLetter.scrollBy(dx, dy)
                }
            }
        })
        rvLetter.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private val manager = rvLetter.layoutManager as LinearLayoutManager
            private val list = arrayListOf<String>()
            private var firstLast = -1
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (RecyclerView.SCROLL_STATE_IDLE != recyclerView.scrollState) {
                    rv.scrollBy(dx, dy)
                }
                val first = manager.findFirstVisibleItemPosition()
                if (firstLast == first) {
                    return
                }
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
        val function: (v: View, event: MotionEvent) -> Boolean = { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    rv.stopScroll()
                    rvLetter.stopScroll()
                }
            }
            false
        }
        rv.setOnTouchListener(function)
        rvLetter.setOnTouchListener(function)
    }

    private fun handleItems(
        items: ArrayList<IconBean>,
        func: (letters: ArrayList<String>, beans: ArrayList<IconBean>) -> Unit
    ) {
        ThreadHelper.getExecutor().execute {
            items.sortWith(kotlin.Comparator { o1, o2 ->
                if (o1.pinyin == null && o2.pinyin == null) {
                    return@Comparator 0
                }
                if (o1.pinyin == null) {
                    return@Comparator 1
                }
                if (o2.pinyin == null) {
                    return@Comparator -1
                }
                return@Comparator o1.pinyin!!.compareTo(o2.pinyin!!)
            })
            val letters = arrayListOf<String>()
            val beans = arrayListOf<IconBean>()
            var index = 0
            items.forEach {
                if (it.letter != null) {
                    if (!letters.contains(it.letter.toString())) {
                        val i = index % count
                        if (i != 0) {
                            for (dis in i until count) {
                                beans.add(IconBean.newContentEmpty())
                                index++
                            }
                        }
                        letters.add(it.letter.toString())
                    } else {
                        if (index % count == 0) {
                            letters.add("")
                        }
                    }
                    beans.add(it)
                    index++
                }
            }
            func.invoke(letters, beans)
        }
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
                            this@TestRecyclerViewActivity,
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
            log(letter)

            val i = map[letter]
            if (i != null) {
                rvLetter.smoothScrollToPosition(i)
            }
            true
        }

        letterNavigation.selectEndListener = { _, _ ->
            teardrop?.visibility = View.GONE
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
        val type: Int,
        val letter: Char? = null,
        var name: String? = null,
        //保留字符的全部拼音，用作同音排序
        var pinyin: String? = null,
        var icon: String? = null
    ) {
        companion object {
            const val TYPE_CONTENT = 1
            const val TYPE_CONTENT_EMPTY = 2

            fun newContentEmpty() = IconBean(TYPE_CONTENT_EMPTY)
            fun newContent(name: String): IconBean {
                val c = name.toCharArray()[0]
                val py = Pinyin.toPinyin(c)
                return if (py == c.toString() || py.isEmpty()) {
                    IconBean(TYPE_CONTENT, '#', name)
                } else {
                    IconBean(TYPE_CONTENT, py.toCharArray()[0], name, py)
                }
            }

            private val chars = "世界上最古老的文字之一已有六千456多年的历史在形体上逐渐由图形变为笔画".split("")

            fun newInstance(): IconBean {
                return newContent("${chars[Random.nextInt(27)]}${Random.nextInt(1234)}")
            }
        }
    }

    private class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    private class LetterAdapter : RecyclerView.Adapter<IconViewHolder>() {

        private val beans: ArrayList<String> = arrayListOf()

        fun setData(letters: java.util.ArrayList<String>) {
            beans.clear()
            beans.addAll(letters)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
            return IconViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_item_recycler_view_letter, parent, false)
            )
        }

        override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
            holder.itemView.findViewById<TextView>(R.id.item_icon_letter).text = beans[position]
        }

        override fun getItemCount() = beans.size
    }

    private class IconAdapter : RecyclerView.Adapter<IconViewHolder>() {

        private val beans: ArrayList<IconBean> = arrayListOf()

        fun setData(list: ArrayList<IconBean>) {
            beans.clear()
            beans.addAll(list)
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return beans[position].type
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
            return IconViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_item_recycler_view_content, parent, false)
            )
        }

        override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
            when (holder.itemViewType) {
                IconBean.TYPE_CONTENT_EMPTY -> {
                    holder.itemView.findViewById<ImageView>(R.id.item_icon_img).background = null
                }
                IconBean.TYPE_CONTENT -> {
                    val item = beans[position]
                    holder.itemView.findViewById<TextView>(R.id.item_icon_name).text = item.name
                    holder.itemView.findViewById<ImageView>(R.id.item_icon_img).apply {
                        background = ColorDrawable(
                            ContextCompat.getColor(
                                this.context,
                                R.color.colorPrimary
                            )
                        )
                    }
                }
            }
        }

        override fun getItemCount() = beans.size
    }
}