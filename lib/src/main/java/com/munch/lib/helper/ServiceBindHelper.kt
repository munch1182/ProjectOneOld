package com.munch.lib.helper

import android.app.Application
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import androidx.core.app.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.BaseApp
import com.munch.lib.helper.ServiceBindHelper.Companion.newBinder
import com.munch.lib.helper.ServiceBindHelper.SimpleServiceBinder

/**
 * service通用绑定，构建一个[SimpleServiceBinder]作为桥梁直接调用[S]里的方法来简化绑定操作
 * 需要[S]的方法[Service.onBind]返回[newBinder]
 * 通过[getService]来直接调用[S]里的方法
 *
 * @param owner 用于自动绑定和解绑，如为null，需要手动调用[bind]和[unbind]，
 * 另外，如果在[Lifecycle.Event.ON_CREATE]之后创建本类，比如使用了[lazy]之类延迟初始化的，需要自行使用[bind]
 * 参见init方法内实现
 *
 * 用bind方式的service在bind组件生命周期外必须解绑，否则会内存泄漏
 *
 * Create by munch1182 on 2020/12/28 10:06.
 */
class ServiceBindHelper<S : Service>(
    private val context: Context,
    private val owner: LifecycleOwner?,
    private val intent: Intent
) {

    companion object {

        fun <S : Service> newBinder(service: S) = SimpleServiceBinder(service)

        fun <S : Service> bindActivity(activity: ComponentActivity, intent: Intent) =
            ServiceBindHelper<S>(activity, activity, intent)

        inline fun <reified S : Service> bindActivity(activity: ComponentActivity) =
            bindActivity<S>(activity, Intent(activity, S::class.java))

        /**
         * @param pkgName 在activity的类初始化过程中，无法通过[ComponentActivity.getPackageName]获取[pkgName]
         */
        fun <S : Service> bindActivity(
            activity: ComponentActivity,
            clazz: Class<S>,
            pkgName: String = BaseApp.getContext().packageName
        ) =
            bindActivity<S>(activity, Intent().setComponent(ComponentName(pkgName, clazz.name)))

        fun <S : Service> bindActivity(activity: ComponentActivity, action: String) =
            bindActivity<S>(activity, Intent(action))

        fun <S : Service> bindApp(app: Application, intent: Intent) =
            ServiceBindHelper<S>(app, null, intent)

        fun <S : Service> bindApp(app: Application, clazz: Class<S>) =
            bindApp<S>(app, Intent(app, clazz))

        fun <S : Service> bindApp(app: Application, action: String) =
            bindApp<S>(app, Intent(action))
    }


    private var service: S? = null
    private var conn: ServiceConnection? = null
    private var binder: SimpleServiceBinder<S>? = null
    private var isBind = false
    private var onBind: ((service: S) -> Unit)? = null

    init {
        owner?.obWhenCreate(onCreate = { bind() }, onDestroy = { unbind() })
    }

    fun onBind(onBind: ((service: S) -> Unit)): ServiceBindHelper<S> {
        this.onBind = onBind
        return this
    }

    fun isBind() = isBind

    fun unbind() {
        if (conn != null) {
            context.unbindService(conn!!)
        }
        isBind = false
        binder?.clear()
        binder = null
        service = null
        conn = null
    }

    fun bind(): ServiceBindHelper<S> {
        if (conn != null) {
            return this
        }
        conn = object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: android.os.IBinder?
            ) {
                @Suppress("UNCHECKED_CAST")
                if (service is SimpleServiceBinder<*>) {
                    binder = service as SimpleServiceBinder<S>
                    this@ServiceBindHelper.service = binder?.get()
                    isBind = true
                    onBind?.invoke(this@ServiceBindHelper.service!!)
                } else {
                    throw Exception("unsupported")
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isBind = false
            }

        }
        context.bindService(intent, conn!!, Service.BIND_AUTO_CREATE)
        return this
    }

    /**
     * 服务的调用必须在确保已经绑定之后
     */
    fun getService(): S? = service

    class SimpleServiceBinder<T : Service> constructor(private var service: T?) : Binder() {

        fun get(): T? {
            return service
        }

        fun clear() {
            service = null
        }
    }
}