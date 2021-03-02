package com.munch.project.launcher.appitem

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.project.launcher.app.task.AppItemHelper
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/2/24 9:02.
 */
class AppViewModel @ViewModelInject constructor() : ViewModel() {

    private val appList = MutableLiveData<MutableList<AppShowBean>>(arrayListOf())
    fun getAppList(): LiveData<MutableList<AppShowBean>> = appList
    private var spanCount = MutableLiveData(4)
    fun getSpanCount(): LiveData<Int> = spanCount
    private var navItems = MutableLiveData<LinkedHashMap<Char, Int>>(linkedMapOf())
    fun getNavItems(): LiveData<LinkedHashMap<Char, Int>> = navItems

    init {
        viewModelScope.launch {
            updateAppShow()
        }
    }

    fun updateAppShow() {
        val instance = AppItemHelper.getInstance()
        appList.postValue(instance.getApps())
        navItems.postValue(instance.getLetterMap())
        spanCount.postValue(instance.getSpanCount())
    }
}