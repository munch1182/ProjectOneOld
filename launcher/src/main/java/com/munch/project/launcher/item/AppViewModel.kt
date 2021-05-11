package com.munch.project.launcher.item

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.pre.lib.extend.toLiveData
import com.munch.project.launcher.base.DataHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.HashMap

/**
 * Create by munch1182 on 2021/5/9 15:52.
 */
class AppViewModel : ViewModel() {

    private val items = MutableLiveData<Pair<MutableList<AppGroupItem>, HashMap<Char, Int>>>()
    fun getItems() = items.toLiveData()
    private val spanCount = MutableLiveData(4)
    fun getSpanCount() = spanCount.toLiveData()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val appSpan = getAndSetSpan()
            val list = scanFromPhone()
            items.postValue(splitBySpan(list, appSpan))
        }
    }

    private suspend fun scanFromPhone(): List<AppGroupItem> {
        return AppItemHelper.getItems().map {
            AppGroupItem(it.info.showName, it.info.showIcon, it.info.pkgName, it.info.launch)
        }.sorted()
    }

    private fun getAndSetSpan(): Int {
        val appSpan = DataHelper.getSpanCount()
        if (appSpan != spanCount.value) {
            spanCount.postValue(appSpan)
        }
        return appSpan
    }

    private fun splitBySpan(
        list: List<AppGroupItem>,
        appSpan: Int
    ): Pair<MutableList<AppGroupItem>, HashMap<Char, Int>> {
        val array = mutableListOf<AppGroupItem>()
        var last = ' '
        var charIndex = -1
        val map = linkedMapOf<Char, Int>()
        list.forEach {
            charIndex++
            //如果该数据的char与上一个char不同，则需要另起一组
            if (it.letter != last && charIndex != 0) {
                //给每一组最后一个位置添加一个占据剩余整行的空数据来占位置，需要GridLayoutManager.SpanSizeLookup配合
                //此处计算上一个位置需要添加的空行数
                val i = appSpan - (charIndex - 1) % appSpan - 1
                //如果该组最后一个位置也是该行最后一个则不添加
                if (i != 0) {
                    array.add(AppGroupItem.empty(last, charIndex, i))
                }
                charIndex = 0
            }
            last = it.letter.toUpperCase()
            it.indexInLetter = charIndex
            array.add(it)
            if (charIndex == 0) {
                map[it.letter] = array.size - 1
            }
        }
        return Pair(array, map)
    }
}