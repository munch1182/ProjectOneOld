package com.munch.project.one.recyclerview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.lib.android.extend.immutable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.munch.project.one.recyclerview.RecyclerIntent as Intent
import com.munch.project.one.recyclerview.RecyclerState as State

/**
 * Create by munch1182 on 2022/9/24 14:31.
 */
class RecyclerVM : ViewModel() {

    private val _state: MutableLiveData<State> = MutableLiveData()
    val state = _state.immutable()
    private val _intent = MutableSharedFlow<Intent>()
    private val repo = RecyclerRepo

    init {
        viewModelScope.launch {
            _intent.collect {
                // 极简写法, 直接在VM中使用来自View的对象方法
                when (it) {
                    Intent.Clear -> operate { set(null) }
                    Intent.NewData -> operate { set(repo.newRandomList()) }
                    is Intent.Remove -> operate { remove(it.index) }
                    Intent.AddList -> operate {
                        val index = Random.nextInt(getItemCount())
                        add(index, repo.newRandomList(getItemCount()))
                        moveTo(index)
                    }
                    Intent.AddOne -> operate {
                        val index = Random.nextInt(getItemCount())
                        add(index, repo.newRandomData(getItemCount()))
                        moveTo(index)
                    }
                    is Intent.Update -> operate {
                        val oldId = get(it.index).id
                        update(it.index, repo.newRandomData(oldId))
                    }
                    is Intent.AddAdd -> addWhenHighFrequency()
                }
            }
        }
    }

    /**
     * 高频添加 测试Adapter的add方法
     */
    private fun addWhenHighFrequency() {
        viewModelScope.launch(Dispatchers.Default) {
            operate { set(null) } // 先清空, 因为这里无法获取当前的id, 所以清空让其等于0

            delay(800L) // 等待dialog隐藏

            val channel = Channel<RecyclerData>()

            launch(Dispatchers.Default) {
                repeat(Random.nextInt(10) * 20 + 10) {
                    // 随机延迟10~260 ms, 但是不会并发
                    delay(10L + (Random.nextInt(50)) * 5)
                    channel.send(repo.newRandomData(it))
                }
                channel.close()
            }

            for (i in channel) {
                operate { add(0, i) }
            }
        }
    }

    fun dispatch(intent: Intent) = viewModelScope.launch { _intent.emit(intent) }

    private fun post(state: State) = _state.postValue(state)

    private fun operate(op: RecyclerAdapterDataFun.() -> Unit) = post(State.Operate(op))
}