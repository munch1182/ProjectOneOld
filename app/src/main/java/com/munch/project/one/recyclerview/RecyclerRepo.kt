package com.munch.project.one.recyclerview

import com.munch.lib.fast.view.newRandomString
import kotlin.random.Random

/**
 * Create by munch1182 on 2022/9/26 16:03.
 */
object RecyclerRepo {

    private var curr = RecyclerIntent.TYPE_NORMAL

    fun newRandomList(start: Int = 0) = when (curr) {
        RecyclerIntent.TYPE_MULTI -> newMultiRandomList(start)
        RecyclerIntent.TYPE_NODE -> newNodeRandomList(start)
        else -> newNormalRandomList(start)
    }

    fun newRandomData(id: Int) = when (curr) {
        RecyclerIntent.TYPE_MULTI -> newMultiRandomData(id)
        RecyclerIntent.TYPE_NODE -> newNodeRandomData(id)
        else -> newNormalRandomData(id)
    }

    fun setDataType(type: Int) {
        if (type == RecyclerIntent.TYPE_SAME) return
        curr = type
    }

    private fun newNodeRandomList(start: Int = 0): MutableList<RecyclerData> {
        return MutableList((Random.nextInt(9) + 1) * 12) { newNodeRandomData(start + it) }
    }

    private fun newNodeRandomData(id: Int): RecyclerData {
        val title = RecyclerDataTitle(id, newRandomString(3 + Random.nextInt(6)))

        val list = MutableList((Random.nextInt(9) + 1) * 12) {
            RecyclerDataContent(id * 1000 + it, newRandomString(3 + Random.nextInt(6)))
        }
        title.setChild(list)
        return title
    }

    private fun newMultiRandomList(start: Int = 0): MutableList<RecyclerData> {
        return MutableList((Random.nextInt(9) + 1) * 12) { newMultiRandomData(start + it) }
    }

    private fun newMultiRandomData(id: Int): RecyclerData {
        return when (Random.nextInt(3)) {
            0 -> RecyclerDataTitle(id, newRandomString(3 + Random.nextInt(6)))
            1 -> RecyclerDataBlank(id)
            2 -> RecyclerDataContent(id, newRandomString(3 + Random.nextInt(6)))
            else -> RecyclerDataContent(id, newRandomString(3 + Random.nextInt(6)))
        }
    }

    private fun newNormalRandomList(start: Int = 0): MutableList<RecyclerData> {
        return MutableList((Random.nextInt(9) + 1) * 12) { newNormalRandomData(start + it) }
    }

    private fun newNormalRandomData(id: Int): RecyclerData {
        return RecyclerData(id, newRandomString(3 + Random.nextInt(6)))
    }
}