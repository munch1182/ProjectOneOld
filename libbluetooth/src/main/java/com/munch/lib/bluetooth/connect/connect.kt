package com.munch.lib.bluetooth.connect

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import com.munch.lib.android.extend.SealedClassToStringByName
import com.munch.lib.bluetooth.helper.BluetoothHelperConfig
import java.util.*

/**
 * Create by munch1182 on 2022/10/27 17:50.
 */

interface IBluetoothConnectState {

    /**
     * 当前设备的连接状态
     */
    val connectState: BluetoothConnectState

    /**
     * 添加连接状态回调
     */
    fun addConnectListener(l: OnBluetoothConnectStateListener)
    fun removeConnectListener(l: OnBluetoothConnectStateListener)
}

interface IBluetoothConnectFun {
    /**
     * 在[timeout]内的连接结果, 仅限此次连接, 当连接成功后的状态变化与此无关
     *
     * @param timeout 连接超时时间, 该时间只对系统连接时间限制, 当系统连接后, 自定义操作不再限制超时时间, 或者说, 需要自行限定超时时间
     * @param config 单独对从设备的连接进行设置, 而不使用全局的连接设置
     * @return 此次连接结果
     *
     * @see com.munch.lib.bluetooth.BluetoothHelper.configConnect
     * @see com.munch.lib.bluetooth.dev.BluetoothDev.addConnectListener
     * @see com.munch.lib.bluetooth.dev.BluetoothDev.removeConnectListener
     */
    suspend fun connect(
        timeout: Long = BluetoothHelperConfig.builder.defaultTimeout,
        config: BluetoothConnector.Builder? = null
    ): BluetoothConnectResult

    suspend fun connect(config: BluetoothConnector.Builder) = connect(
        BluetoothHelperConfig.builder.defaultTimeout, config
    )

    /**
     * @param removeBond 是否需要移除系统的绑定
     *
     * @return 当[removeBond]为true时, 该结果返回是否解除成功, 否则固定返回true
     */
    suspend fun disconnect(removeBond: Boolean): Boolean
}

interface IBluetoothConnector : IBluetoothConnectState, IBluetoothConnectFun

/**
 * 提供连接相关的操作
 */
interface IBluetoothHelperConnector {

    /**
     * 设置通用的连接设置
     */
    fun configConnect(build: BluetoothConnector.Builder.() -> Unit)
}

/**
 * 提供名字
 */
interface BluetoothConnector {
    class Builder {
        //<editor-fold desc="LE">
        internal var transport: Int = BluetoothDevice.TRANSPORT_AUTO
        internal var phy: Int = BluetoothDevice.PHY_LE_1M_MASK
        internal var judge: IBluetoothConnectJudge? = null

        /**
         * 连接时的参数
         */
        fun transport(transport: Int): Builder {
            this.transport = transport
            return this
        }

        /**
         * 连接时的参数
         */
        fun phy(phy: Int): Builder {
            this.phy = phy
            return this
        }

        /**
         * 连接流程的处理
         *
         * @see IBluetoothConnectJudge
         */
        fun judge(vararg judge: IBluetoothConnectJudge): Builder {
            if (judge.isEmpty()) {
                this.judge = null
            } else if (judge.size == 1) {
                this.judge = judge.first()
            } else {
                this.judge = BluetoothConnectJudgeContainer(*judge)
            }
            return this
        }
        //</editor-fold>

        //<editor-fold desc="CLASSIC">
        internal var name: String = "p1"
        internal var uuid = UUID.randomUUID()

        fun name(name: String): Builder {
            this.name = name
            return this
        }

        fun uuid(uuid: UUID): Builder {
            this.uuid = uuid
            return this
        }
        //</editor-fold>
    }
}

/**
 * 当本库执行完系统连接并连接成功后, 会回调此接口用以执行传入的自定义操作
 * 如果自定义操作返回连接成功, 则本库会视为连接成功并回调连接成功
 * 否则, 会方法此自定义的连接结果并视作连接失败, 并自动断开连接
 */
fun interface IBluetoothConnectJudge {
    suspend fun onJudge(gatt: BluetoothGattHelper): BluetoothConnectResult
}

class BluetoothConnectJudgeContainer(vararg judges: IBluetoothConnectJudge) :
    IBluetoothConnectJudge {
    private val list = mutableListOf(*judges)

    fun add(judge: IBluetoothConnectJudge): BluetoothConnectJudgeContainer {
        list.add(judge)
        return this
    }

    fun remove(judge: IBluetoothConnectJudge): BluetoothConnectJudgeContainer {
        list.remove(judge)
        return this
    }

    override suspend fun onJudge(gatt: BluetoothGattHelper): BluetoothConnectResult {
        list.forEach {
            val judge = it.onJudge(gatt)
            if (!judge.isSuccess) return judge
        }
        return BluetoothConnectResult.Success
    }

}

/**
 * 连接结果
 */
sealed class BluetoothConnectResult : SealedClassToStringByName() {
    object Success : BluetoothConnectResult() {
        override fun toString() = "ConnectSuccess"
    }

    class Fail(val reason: IBluetoothConnectFailReason) : BluetoothConnectResult() {
        override fun toString() = "ConnectFail($reason)"
    }

    val isSuccess: Boolean
        get() = this is Success
}

/**
 * 连接状态
 */
sealed class BluetoothConnectState : SealedClassToStringByName() {
    /**
     * 未连接
     */
    object Disconnected : BluetoothConnectState()

    /**
     * 连接中
     *
     * 注意: 此连接中可能为系统未回调已连接时的阶段, 也可能时系统已连接但自定义操作未完成的阶段, 需要看具体实现的类
     */
    object Connecting : BluetoothConnectState()

    /**
     * 已连接
     */
    object Connected : BluetoothConnectState()

    object Disconnecting : BluetoothConnectState()

    val isConnected: Boolean
        get() = this is Connected
    val isConnecting: Boolean
        get() = this is Connecting
    val isDisconnected: Boolean
        get() = this is Disconnected
    val isDisconnecting: Boolean
        get() = this is Disconnecting

    companion object {
        fun from(state: Int): BluetoothConnectState {
            return when (state) {
                BluetoothGatt.STATE_CONNECTED -> Connected
                BluetoothGatt.STATE_DISCONNECTED -> Disconnected
                BluetoothGatt.STATE_CONNECTING -> Connecting
                BluetoothGatt.STATE_DISCONNECTING -> Disconnecting
                else -> Disconnected
            }
        }
    }
}

/**
 * 连接状态回调
 */
fun interface OnBluetoothConnectStateListener {
    fun onConnectState(state: BluetoothConnectState, last: BluetoothConnectState)
}

/**
 * 连接结果失败的原因, 使用接口用于自定义拓展
 */
interface IBluetoothConnectFailReason {
    val code: Int
}

/**
 * 默认的连接失败的原因
 */
sealed class BluetoothConnectFailReason : SealedClassToStringByName(), IBluetoothConnectFailReason {
    object MacInvalid : BluetoothConnectFailReason() // 该mac地址无效
    object NotFindDev : BluetoothConnectFailReason() // 连接前未扫描到该设备
    object ConnectTimeout : BluetoothConnectFailReason() // 连接超时
    object ConnectedButConnect : BluetoothConnectFailReason() // 已连接仍调用连接
    class SysErr(private val sysErrCode: Int) : BluetoothConnectFailReason() { // 系统回调错误
        override val code: Int
            get() = sysErrCode

        override fun toString() = "SysErr($sysErrCode)"
    }

    class CustomErr(private val customCode: Int) : BluetoothConnectFailReason() {
        override val code: Int
            get() = customCode

        override fun toString() = "CustomErr($customCode)"
    }

    fun toReason() = BluetoothConnectResult.Fail(this)

    override val code: Int
        get() = 999 // 自定义的code都是999, 否则则应该是系统返回的code
}

internal class BluetoothStateNotify {


}
