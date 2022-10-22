package com.munch.lib.android.extend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.lib.android.define.Notify
import com.munch.lib.android.helper.ILifecycle
import com.munch.lib.android.helper.MutableLifecycle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2022/10/22 13:47.
 */
abstract class LifeVM : ViewModel(), ILifecycle {
    private val life = MutableLifecycle()

    init {
        life.active()
    }

    override fun onCleared() {
        super.onCleared()
        life.inactive()
    }

    override fun onActive(onCreate: Notify) {
        life.onActive(onCreate)
    }

    override fun onInactive(onDestroy: Notify) {
        life.onInactive(onDestroy)
    }
}

abstract class ContractVM<INTENT, STATE> : LifeVM() {

    private val _state: MutableLiveData<STATE> = MutableLiveData()
    val state: LiveData<STATE>
        get() = _state
    private val _intent = MutableSharedFlow<INTENT>()

    init {
        viewModelScope.launch { _intent.collect { onCollect(it) } }
    }

    protected abstract suspend fun onCollect(it: INTENT)

    fun dispatch(intent: INTENT) = viewModelScope.launch { _intent.emit(intent) }

    protected fun post(state: STATE) {
        _state.postValue(state)
    }
}