package com.munch.project.one.recyclerview

import com.munch.lib.android.extend.SealedClassToStringByName
import com.munch.lib.android.recyclerview.AdapterDataFun

/**
 * Create by munch1182 on 2022/9/24 14:32.
 */
sealed class RecyclerIntent : SealedClassToStringByName() {
    object Set : RecyclerIntent()
    object Add : RecyclerIntent()
    object AddMore : RecyclerIntent()
    class Update(val pos: Int) : RecyclerIntent()
    object ChangeType : RecyclerIntent()
    object Clear : RecyclerIntent()
}

sealed class RecyclerState : SealedClassToStringByName() {

    object None : RecyclerState()
    object Loading : RecyclerState()
    class Data(val data: List<RecyclerData>) : RecyclerState()
    class Execute(val exe: AdapterDataFun<RecyclerData>.() -> Unit) : RecyclerState()
}

data class RecyclerData(val id: String, val data: String) {
    override fun hashCode() = id.toInt()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RecyclerData
        if (id != other.id) return false
        return true
    }
}