package com.munch.lib.bluetooth

/**
 *
 * 蓝牙当前活动及状态管理
 *
 * Create by munch1182 on 2021/12/3 15:18.
 */
class BluetoothStateHelper {

    private val log = BluetoothHelper.logHelper
    private val stateLock = Object()
    private var state: State = State.UNKNOWN
        get() = synchronized(stateLock) { field }
        set(value) {
            synchronized(stateLock) {
                if (field != value) {
                    val old = field
                    field = value
                    log.withEnable { "state: $old -> $field" }
                }
            }
        }
    val currentState: State
        get() = state

    /**
     * 判断当前是否能进行扫描或者连接操作
     *
     * 当蓝牙进行一项操作未结束时，不要进行下一次操作
     */
    val canOp: Boolean
        get() = state == State.IDLE

    val isUNKNOWN: Boolean
        get() = state == State.UNKNOWN
    val isIDLE: Boolean
        get() = state == State.IDLE
    val isSCANNING: Boolean
        get() = state == State.SCANNING
    val isCONNECTING: Boolean
        get() = state == State.CONNECTING
    val isDISCONNECTING: Boolean
        get() = state == State.DISCONNECTING

    fun updateUNKNOWNState() = updateState(State.UNKNOWN)
    fun updateIDLEState() = updateState(State.IDLE)
    fun updateCLOSEState() = updateState(State.CLOSED)
    fun updateSCANNINGState() = updateState(State.SCANNING)
    fun updateCONNECTINGState() = updateState(State.CONNECTING)
    fun updateDISCONNECTINGState() = updateState(State.DISCONNECTING)

    private fun updateState(state: State) {
        this.state = state
    }
}

sealed class State {

    /**
     * 刚初始化，不支持蓝牙，或者没有权限，对蓝牙状态未知
     */
    object UNKNOWN : State()

    /**
     * 蓝牙已关闭
     */
    object CLOSED : State()

    /**
     * 蓝牙已打开，且未进行任何其它操作
     */
    object IDLE : State()

    /**
     * 蓝牙正在进行扫描
     */
    object SCANNING : State()

    /**
     * 蓝牙正在连接某个设备
     */
    object CONNECTING : State()

    /**
     * 蓝牙正在断开某个设备
     */
    object DISCONNECTING : State()

}