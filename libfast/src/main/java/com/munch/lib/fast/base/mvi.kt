package com.munch.lib.fast.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.lib.extend.lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2022/6/22 20:21.
 */

interface StateEventDispatcher<STATE, EVENT> {
    val state: StateFlow<STATE>
    fun dispatch(event: EVENT)
}

interface MutableStateEventDispatcher<STATE, EVENT> : StateEventDispatcher<STATE, EVENT> {
    override val state: StateFlow<STATE>
    val event: SharedFlow<EVENT>

    fun update(action: STATE)
    override fun dispatch(event: EVENT)
}

class MutableStateEventDispatcherImp<STATE, EVENT>(
    initialState: STATE,
    private val scope: CoroutineScope
) : MutableStateEventDispatcher<STATE, EVENT> {

    private val _state: MutableStateFlow<STATE> = MutableStateFlow(initialState)
    override val state: StateFlow<STATE> = _state
    private val _event = MutableSharedFlow<EVENT>()
    override val event: SharedFlow<EVENT> = _event

    override fun dispatch(event: EVENT) {
        scope.launch(Dispatchers.Default) { _event.emit(event) }
    }

    override fun update(action: STATE) {
        scope.launch(Dispatchers.Default) { _state.emit(action) }
    }
}

fun <STATE, EVENT> ViewModel.dispatcher(initialState: STATE): Lazy<MutableStateEventDispatcherImp<STATE, EVENT>> =
    lazy { MutableStateEventDispatcherImp(initialState, viewModelScope) }

@Suppress("NOTHING_TO_INLINE")
inline fun <STATE, EVENT> MutableStateEventDispatcherImp<STATE, EVENT>.toLive(): StateEventDispatcher<STATE, EVENT> =
    this