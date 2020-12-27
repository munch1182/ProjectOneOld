package com.munch.project.test.view

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import com.munch.lib.log
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.test.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * Create by munch1182 on 2020/12/24 17:24.
 */
class TestRecyclerViewActivity : TestBaseTopActivity() {

    private val letterNavigation: LetterNavigationBarView by lazy { findViewById(R.id.view_letter_navigation) }
    private val rv: RecyclerView by lazy { findViewById(R.id.view_letter_rv) }
    private val container: ConstraintLayout by lazy { findViewById(R.id.view_letter_container) }
    private var teardrop: TeardropAngleView? = null

    companion object {
        private const val COUNT = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_recycler_view)
        letterNavigation.setAllLetters()
        letterNavigation.select("A")

        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(applicationContext)))

        rv.layoutManager = StaggeredGridLayoutManager(COUNT, StaggeredGridLayoutManager.VERTICAL)
        rv.adapter = IconAdapter(getItems(), rv)

        handleLetterNavigation()
    }

    private fun handleLetterNavigation() {
        letterNavigation.handleListener = { letter, rect ->
            val height = 240
            var marginBottom = 0
            var marginTop = rect.top + rect.height() / 2 - height / 2
            log(marginTop)
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
            letterNavigation.select(letter)
            true
        }

        letterNavigation.selectEndListener = { _, _ ->
            teardrop?.visibility = View.GONE
        }
    }

    private fun getItems(): ArrayList<IconContentBean> {
        val arrayList = ArrayList<IconContentBean>()
        for (i in 0..100) {
            arrayList.add(IconContentBean.newInstance())
        }
        return arrayList
    }

    data class IconBean(val type: Int, val item: Any) {
        companion object {
            const val TYPE_TITLE = 0
            const val TYPE_CONTENT = 1

            fun newTitle(bean: IconTitleBean) = IconBean(TYPE_TITLE, bean)
            fun newContent(bean: IconContentBean) = IconBean(TYPE_CONTENT, bean)
        }
    }

    data class IconTitleBean(val letter: Char)
    data class IconContentBean(val name: String, val icon: Any?, var letter: String?) {

        companion object {
            private val chars =
                "大江东去浪淘尽千古风流人物故垒西边人道是三国周郎赤壁乱石穿空惊涛拍岸卷起千堆雪江山如画一时多少豪杰遥想公瑾当年小乔初嫁了雄姿英发".split("")

            fun newInstance(): IconContentBean {
                return IconContentBean(
                    chars[Random.nextInt(64)].plus(Random.nextInt(1234)),
                    null,
                    null
                )
            }
        }

    }

    private class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    private class IconAdapter(
        private val items: ArrayList<IconContentBean>?,
        private val rv: View
    ) :
        RecyclerView.Adapter<IconViewHolder>() {

        init {
            handleItems()
        }

        private val beans: ArrayList<IconBean> = arrayListOf()

        private fun handleItems() {
            thread {
                val beansThread: ArrayList<IconBean> = arrayListOf()
                items ?: return@thread
                items.forEach {
                    var toPinyin =
                        Pinyin.toPinyin(it.name.toCharArray(0, 1)[0]).toLowerCase(Locale.ROOT)
                    if (toPinyin.isEmpty()) {
                        toPinyin = "#"
                    }
                    try {
                        toPinyin.toDouble()
                        toPinyin = "#"
                    } catch (e: Exception) {
                        //数字放到#中
                    }
                    it.letter = toPinyin
                }
                Collections.sort(items, object : Comparator<IconContentBean> {
                    override fun compare(o1: IconContentBean?, o2: IconContentBean?): Int {
                        if (o1 == null && o2 == null) {
                            return 0
                        }
                        o1 ?: return 1
                        o2 ?: return -1
                        o1.letter?.takeIf { it != "#" } ?: return 1
                        o2.letter?.takeIf { it != "#" } ?: return -1
                        return o1.letter!!.compareTo(o2.letter!!)
                    }
                })
                var letter = '?'
                var index = 0
                items.forEach {
                    val i = index % COUNT
                    val c = it.letter!!.toCharArray(0, 1)[0]
                    //新的标题
                    if (letter != c) {
                        letter = c
                        if (i != 0) {
                            for (more in i until COUNT) {
                                //占位
                                beansThread.add(
                                    IconBean.newContent(IconContentBean("", null, null))
                                )
                                index++
                            }
                        }
                        beansThread.add(IconBean.newTitle(IconTitleBean(letter)))
                    } else {
                        //占位
                        if (i == 0) {
                            beansThread.add(IconBean.newTitle(IconTitleBean('?')))
                            index++
                        }
                        beansThread.add(IconBean.newContent(it))
                    }
                    index++
                }
                beans.clear()
                beans.addAll(beansThread)
                rv.post {
                    notifyDataSetChanged()
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return beans[position].type
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
            return when (viewType) {
                IconBean.TYPE_CONTENT -> IconViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_item_recycler_view_content, parent, false)
                )
                else -> IconViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_item_recycler_view_letter, parent, false)
                )
            }
        }

        override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
            when (holder.itemViewType) {
                IconBean.TYPE_CONTENT -> {
                    val item = (beans[position].item as IconContentBean)
                    holder.itemView.findViewById<TextView>(R.id.item_icon_name).text =
                        item.name
                    if (item.name.isEmpty()) {
                        holder.itemView.findViewById<ImageView>(R.id.item_icon_img).background =
                            null
                    } else {
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
                IconBean.TYPE_TITLE -> {
                    var toString = (beans[position].item as IconTitleBean).letter.toString()
                    if ("?" == toString) {
                        toString = ""
                    }
                    holder.itemView.findViewById<TextView>(R.id.item_icon_letter).text =
                        toString
                }
            }
        }

        override fun getItemCount(): Int {
            return items?.size ?: 0
        }
    }
}