package com.munch.project.launcher.app

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/2/24 9:02.
 */
class AppViewModel @ViewModelInject constructor(private val repository: AppRepository) :
    ViewModel() {

    private val appList = MutableLiveData<MutableList<AppShowBean>>(arrayListOf())
    fun getAppList(): LiveData<MutableList<AppShowBean>> = appList
    private var spanCount = MutableLiveData(4)
    fun getSpanCount(): LiveData<Int> = spanCount
    private var navItems = MutableLiveData<LinkedHashMap<Char, Int>>(linkedMapOf())
    fun getNavItems(): LiveData<LinkedHashMap<Char, Int>> = navItems

    private var char: Char = ' '
    private var charIndex = -1

    init {
        viewModelScope.launch {
            updateAppShow()
        }
    }

    private fun updateAppShow() {
        val spanCount = spanCount.value!!
        val apps = repository.queryAppByScan()
            ?.map { AppShowBean.new(it) }
            ?.toMutableList()
            ?: return
        apps.sort()
        val appsInSpace = mutableListOf<AppShowBean>()
        val map = linkedMapOf<Char, Int>()
        apps.forEach {
            charIndex++
            //如果该数据的char与上一个char不同，则需要另起一组
            if (it.letterChar != char && charIndex != 0) {
                //给每一组最后一个位置添加一个占据剩余整行的空数据来占位置，需要GridLayoutManager.SpanSizeLookup配合
                //此处计算上一个位置需要添加的空行数
                val i = spanCount - (charIndex - 1) % spanCount - 1
                //如果该组最后一个位置也是改行最后一个则不添加
                if (i != 0) {
                    val parameter = ShowParameter(charIndex)
                    parameter.space2End = i
                    //add
                    appsInSpace.add(AppShowBean.empty(char, parameter))
                }
                charIndex = 0
            }
            char = it.letterChar
            it.updateShowParameter(ShowParameter(charIndex))
            //add
            appsInSpace.add(it)
            if (charIndex == 0) {
                map[char] = appsInSpace.size - 1
            }
        }
        appList.postValue(appsInSpace)
        navItems.postValue(map)
    }
}