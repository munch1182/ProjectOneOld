package com.munch.lib.weight.shape

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.children
import com.munch.lib.extend.icontext.IContext
import com.munch.lib.weight.ContainerLayout
import com.munch.lib.weight.FunctionalView

class Selector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ContainerLayout(context, attrs, defStyleAttr), FunctionalView, IContext {

    private data class Item(val shape: Shape, val state: Int)

    private val shapes = mutableListOf<Item>()

    private fun collectShape() {
        /*children.forEach {
            if (it is Shape) {
                shapes.add(Item(it, it))
            }
        }*/
    }
}