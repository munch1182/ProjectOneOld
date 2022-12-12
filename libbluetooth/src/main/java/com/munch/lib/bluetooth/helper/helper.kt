package com.munch.lib.bluetooth.helper

import com.munch.lib.android.log.Logger
import com.munch.lib.bluetooth.BluetoothHelper
import com.munch.lib.bluetooth.dev.BluetoothDev
import com.munch.lib.bluetooth.dev.BluetoothType
import com.munch.lib.bluetooth.scan.IBluetoothDevScanner
import com.munch.lib.bluetooth.scan.IBluetoothOnceScanner
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/10/26 16:56.
 */

//<editor-fold desc="env">
/**
 * 提供蓝牙相关的通用方法和环境
 */
internal interface IBluetoothHelperEnv : CoroutineScope {

    /**
     * 提供一个[BluetoothHelper]及其相关类的统一输出对象
     *
     * 这个可以交由外部使用, 以保存统一的日志位置
     */
    val log: Logger

    /**
     * 可以自行实现以添加统一前缀或者后缀
     */
    fun log(content: String) {
        if (BluetoothHelperConfig.builder.enableLog) {
            log.log(content)
        }
    }

    /**
     * 提供一个[BluetoothHelper]及其相关类的统一上下文
     */
    override val coroutineContext: CoroutineContext
}

/**
 * 提供一个唯一的对象供所有需要的相关类使用
 * 因为大多相关类先于[BluetoothHelper]初始化, 所以不能直接使用[BluetoothHelper]对象作为[IBluetoothHelperEnv]
 */
internal object BluetoothHelperEnv : IBluetoothHelperEnv {

    override val log: Logger = Logger.onlyThread("bluetooth")

    private val appJob = SupervisorJob()
    private val appJobName = CoroutineName("bluetooth")

    override val coroutineContext: CoroutineContext = appJob + appJobName + Dispatchers.Default

}
//</editor-fold>

//<editor-fold desc="scan">
/**
 * BluetoothHelper对外提供的扫描方法相关
 * 1. 提供一个默认的扫描器相关方法及其配置方法: 除了[newScanner]之外的方法
 * 2. 提供一个构造新的一次性扫描器的方法: [newScanner]方法
 */
interface IBluetoothHelperScanner : IBluetoothDevScanner {

    /**
     * 返回一个新建一个扫描器进行扫描活动
     * 注意: 该扫描器不同于默认的扫描器, 必须是由返回的对象调用对于方法
     *
     * 新建的扫描器不会影响默认或者其它扫描器的设置、状态和返回
     */
    fun newScanner(builder: BluetoothHelperScanner.Builder): IBluetoothOnceScanner

    /**
     * 对默认扫描器进行设置, 该设置会被使用到之后的扫描中
     */
    fun configDefaultScan(config: BluetoothHelperScanner.Builder.() -> Unit)
}

/**
 * 提供名称
 */
interface BluetoothHelperScanner {
    class Builder {
        internal var type: BluetoothType = BluetoothType.LE

        /**
         * 修改扫描类型, 会停止当前扫描, 但是不会清除回调
         */
        fun type(type: BluetoothType): Builder {
            this.type = type
            return this
        }

        internal var filter: OnBluetoothDevFilter? = null

        /**
         * 对扫描到的设备进行过滤, 被过滤的设备不会回调到回调中, 可以设置多个过滤器
         */
        fun filter(vararg filters: OnBluetoothDevFilter): Builder {
            this.filter = when (filters.size) {
                0 -> null
                1 -> filters.first()
                else -> BluetoothDevFilterContainer(*filters)
            }
            return this
        }

        fun filter(filter: OnBluetoothDevFilter?): Builder {
            if (filter == null) return filter()
            return filter(filter)
        }

        internal var delayTime = 0L

        /**
         * 当回调时, 可以对前后两次回调设置间隔时间, 避免回调过快
         * 如果该值小于等于0, 则不会设置间隔
         *
         * 注意: 当停止扫描时, 仍未回调的设备会被丢弃不再回调
         */
        fun delay(time: Long): Builder {
            this.delayTime = time
            return this
        }

        fun newScanner(): IBluetoothOnceScanner {
            return BluetoothHelper.newScanner(this)
        }
    }
}
//</editor-fold>

//<editor-fold desc="filter">
/**
 * 对该设备进行过滤
 */
fun interface OnBluetoothDevFilter {
    /**
     * 如果需要被过滤掉, 不回调到结果中, 返回true,
     * 否则返回false
     */
    fun isNeedBeFilter(dev: BluetoothDev): Boolean
}

/**
 * 拓展[OnBluetoothDevFilter], 附带开始和结束的回调
 */
interface OnBluetoothDevLifecycleFilter : OnBluetoothDevFilter {
    fun onStart() {}
    fun onStop() {}
}

/**
 * 组合[OnBluetoothDevFilter]成一个
 */
class BluetoothDevFilterContainer(vararg filters: OnBluetoothDevFilter) :
    OnBluetoothDevLifecycleFilter {
    private val list = mutableListOf(*filters)
    override fun isNeedBeFilter(dev: BluetoothDev): Boolean {
        list.forEach { if (it.isNeedBeFilter(dev)) return true } // 任一过滤器需要过滤, 则返回true
        return false
    }

    override fun onStart() {
        super.onStart()
        list.filterIsInstance<OnBluetoothDevLifecycleFilter>().forEach { it.onStart() }
    }

    override fun onStop() {
        super.onStop()
        list.filterIsInstance<OnBluetoothDevLifecycleFilter>().forEach { it.onStop() }
    }

}
//</editor-fold>

interface IBluetoothHelperConfig {

    fun config(config: Builder.() -> Unit)

    class Builder {
        // 是否输出日志
        internal var enableLog = true

        // 是否输出设备扫描的日志
        internal var enableLogDevScan = false
            get() = if (!enableLog) false else field

        // 默认的方法超时时间
        internal var defaultTimeout = 30 * 1000L

        fun enableLog(enable: Boolean): Builder {
            this.enableLog = enable
            return this
        }

        fun enableLogDevScan(enable: Boolean): Builder {
            this.enableLogDevScan = enable
            return this
        }

        fun defaultTimeout(timeout: Long): Builder {
            this.defaultTimeout = timeout
            return this
        }

        // todo 传入service id控制扫码范围
        fun injectScan():Builder{
            return this
        }
    }
}

internal object BluetoothHelperConfig : IBluetoothHelperConfig {

    internal val builder = IBluetoothHelperConfig.Builder()

    override fun config(config: IBluetoothHelperConfig.Builder.() -> Unit) {
        config.invoke(builder)
    }
}