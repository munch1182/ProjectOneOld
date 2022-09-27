package com.munch.project.one.recyclerview

import com.munch.lib.android.extend.SealedClassToStringByName
import com.munch.lib.android.recyclerview.AdapterDataFun

/**
 * Create by munch1182 on 2022/9/24 14:32.
 */
sealed class RecyclerIntent : SealedClassToStringByName() {
    object NextType : RecyclerIntent()
    object NewData : RecyclerIntent() // 设置一个新的数据
    object AddOne : RecyclerIntent() // 添加一个数据
    object AddList : RecyclerIntent() // 添加一组数据
    object Clear : RecyclerIntent() // 清除数据
    object RemoveRange : RecyclerIntent() // 删除一段数据
    class Remove(var index: Int) : RecyclerIntent() // 移除该位置的数据
    class Update(var index: Int) : RecyclerIntent() // 更新该位置的数据

    object AddAdd : RecyclerIntent() // 高频添加

}

sealed class RecyclerState : SealedClassToStringByName() {

    // 让vm直接操作AdapterFunHelper
    class Operate(val op: RecyclerAdapterDataFun.() -> Unit) : RecyclerState()

    object NextType : RecyclerState() // 更换Adapter的类型
}

data class RecyclerData(
    val id: Int, // 生成时的ID, 根据item数量
    val data: String, // 显示的内容
) {
    override fun hashCode() = id
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RecyclerData
        if (id != other.id) return false
        return true
    }
}

interface RecyclerAdapterDataFun : AdapterDataFun<RecyclerData> { // 拓展一个方法, 用于传递数据
    fun moveTo(index: Int)
}