package com.munch.test.project.one.view

import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.fast.base.BaseBindAdapter
import com.munch.lib.fast.base.BaseBindViewHolder
import com.munch.lib.fast.databinding.ActivityBaseRvBinding
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ItemPaintModeBinding

/**
 * Create by munch1182 on 2021/1/12 17:33.
 */
class PaintModeActivity : BaseTopActivity() {

    private val bind by bind<ActivityBaseRvBinding>(R.layout.activity_base_rv)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this@PaintModeActivity
        bind.baseTopRv.apply {
            layoutManager = GridLayoutManager(this@PaintModeActivity, 3)
            setBackgroundColor(Color.WHITE)
            addItemDecoration(object : RecyclerView.ItemDecoration() {

                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    /*super.getItemOffsets(outRect, view, parent, state)*/
                    val gridLayoutManager = parent.layoutManager as? GridLayoutManager? ?: return
                    val space = 5
                    val spanCount = gridLayoutManager.spanCount
                    val position = parent.getChildLayoutPosition(view)
                    val column = position % spanCount
                    outRect.left = space - column * space / spanCount
                    outRect.right = (column + 1) * space / spanCount
                    if (position < spanCount) {
                        outRect.top = space
                    }
                    outRect.bottom = space
                }
            })
            adapter = object : BaseBindAdapter<ModeBean, ItemPaintModeBinding>(
                R.layout.item_paint_mode,
                getData()
            ) {
                override fun onBindViewHolder(
                    holder: BaseBindViewHolder<ItemPaintModeBinding>,
                    bean: ModeBean,
                    pos: Int
                ) {
                    holder.itemView.apply {
                        findViewById<PorterDuffXfermodeView>(R.id.paint_mode_view).changeXfermode(
                            bean.xfermode,
                            bean.close
                        )
                        findViewById<TextView>(R.id.paint_mode_tv).text = bean.name
                    }

                }

            }
        }
    }

    private fun getData() = ModeBean.newData()

    private data class ModeBean constructor(
        val xfermode: Xfermode? = null,
        val name: String? = null,
        val close: Boolean = false
    ) {

        companion object {

            fun newData(): MutableList<ModeBean> {
                return mutableListOf(
                    ModeBean(null, "null"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.CLEAR), "clear"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.CLEAR), "clear*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.SRC), "src"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.SRC), "src*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.DST), "dst"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.DST), "dst*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER), "src over"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER), "src over*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.DST_OVER), "dst over"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.DST_OVER), "dst over*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.SRC_IN), "src in"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.SRC_IN), "src in*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.DST_IN), "dst in"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.DST_IN), "dst in*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.SRC_OUT), "src out"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.SRC_OUT), "src out*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.DST_OUT), "dst out"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.DST_OUT), "dst out*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP), "src atop"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP), "src atop*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.DST_ATOP), "dst atop"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.DST_ATOP), "dst atop*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.XOR), "xor"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.XOR), "xor*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.DARKEN), "darken"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.DARKEN), "darken*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.LIGHTEN), "lighten"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.LIGHTEN), "lighten*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.MULTIPLY), "multiply"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.MULTIPLY), "multiply*", true),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.SCREEN), "screen"),
                    ModeBean(PorterDuffXfermode(PorterDuff.Mode.SCREEN), "screen*", true)
                )
            }
        }
    }
}
