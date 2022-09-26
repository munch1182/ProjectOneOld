package com.munch.project.one.recyclerview

import com.munch.lib.fast.view.newRandomString
import kotlin.random.Random

/**
 * Create by munch1182 on 2022/9/26 16:03.
 */
object RecyclerRepo {

    fun newRandomList(start: Int = 0): MutableList<RecyclerData> {
        return MutableList((Random.nextInt(9) + 1) * 12) { newRandomData(start + it) }
    }

    fun newRandomData(id: Int): RecyclerData {
        return RecyclerData(id, newRandomString(3 + Random.nextInt(6)))
    }
}