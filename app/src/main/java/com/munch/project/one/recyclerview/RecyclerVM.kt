package com.munch.project.one.recyclerview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.lib.android.extend.immutable
import com.munch.lib.android.recyclerview.AdapterDataFun
import com.munch.lib.fast.view.newRandomString
import com.munch.project.one.recyclerview.RecyclerState.Execute
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random
import com.munch.project.one.recyclerview.RecyclerIntent as Intent
import com.munch.project.one.recyclerview.RecyclerState as State

/**
 * Create by munch1182 on 2022/9/24 14:31.
 */
class RecyclerVM : ViewModel() {

    private val _state: MutableLiveData<State> = MutableLiveData(State.None)
    val state = _state.immutable()
    private val _intent = MutableSharedFlow<Intent>()

    init {
        viewModelScope.launch {
            _intent.collect {
                when (it) {
                    Intent.Add -> post(Execute {
                        add(Random.nextInt(randomCount), newRandomData(getItemCount(), "add > "))
                    })
                    Intent.AddMore -> post(Execute {
                        add(Random.nextInt(randomCount), newRandomList(getItemCount(), "add > "))
                    })
                    Intent.ChangeType -> {}
                    Intent.Set -> post(Execute { set(newRandomList()) })
                    is Intent.Update -> post(Execute { update(newRandomData(it.pos)) })
                    Intent.Clear -> post(Execute { set(null) })
                }
            }
        }
    }

    private inline val <D> AdapterDataFun<D>.randomCount: Int
        get() = max(getItemCount(), 1) // 保证Random.nextInt的参数不能为0

    private fun newRandomList(start: Int = 0, prefix: String? = null): List<RecyclerData> {
        return MutableList((Random.nextInt(9) + 1) * 12) { newRandomData(start + it, prefix) }
    }

    private fun newRandomData(int: Int, prefix: String? = null): RecyclerData {
        return RecyclerData(
            int.toString(),
            "${prefix ?: ""}${newRandomString(Random.nextInt(5) + 3)}"
        )
    }

    fun dispatch(intent: Intent) {
        viewModelScope.launch { _intent.emit(intent) }
    }

    private fun post(state: State) {
        _state.postValue(state)
    }
}