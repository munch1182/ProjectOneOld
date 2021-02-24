package com.munch.project.launcher.app

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.project.launcher.db.AppBean
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/2/24 9:02.
 */
class AppViewModel @ViewModelInject constructor(private val repository: AppRepository) :
    ViewModel() {

    private val appList = MutableLiveData<List<AppBean>>(null)
    fun getAppList(): LiveData<List<AppBean>> = appList

    init {
        viewModelScope.launch {
            appList.postValue(repository.queryAppByScan())
        }
    }
}