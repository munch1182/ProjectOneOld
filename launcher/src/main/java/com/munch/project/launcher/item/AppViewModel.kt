package com.munch.project.launcher.item

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.pre.lib.extend.toLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/5/9 15:52.
 */
class AppViewModel : ViewModel() {

    private val items = MutableLiveData<MutableList<AppItem>>(mutableListOf())
    fun getItems() = items.toLiveData()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = AppItemHelper.getItems()
            items.postValue(apps)
        }
    }
}