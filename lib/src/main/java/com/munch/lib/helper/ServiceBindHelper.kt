package com.munch.lib.helper

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import androidx.core.app.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.munch.lib.helper.ServiceBindHelper.Companion.newBinder

/**
 * service通用绑定，需要[Service.onBind]返回[newBinder]，
 * 通过[getService]或者[opService]来直接调用[S]里的方法
 *
 *
 * @param owner 用于自动绑定和解绑，如为null，需要手动调用[bind]和[unbind]，
 * 另外，如果在[Lifecycle.Event.ON_CREATE]之后创建被类，比如使用了[lazy]之类延迟初始化的，需要自行使用[bind]
 * 参见init方法内实现
 *
 * 用bind方式的service在bind组件生命周期外必须解绑，否则会内存泄漏
 *
 * Create by munch1182 on 2020/12/28 10:06.
 */
class ServiceBindHelper<S : Service>
private constructor(
    private val context: Context,
    private val owner: LifecycleOwner?,
) {

    constructor(activity: ComponentActivity, intent: Intent) : this(activity, activity, intent)
    constructor(context: Context, owner: LifecycleOwner? = null, intent: Intent) : this(
        context,
        owner
    ) {
        this.intent = intent
    }

    constructor(activity: ComponentActivity, service: Class<S>) : this(activity, activity, service)
    constructor(context: Context, owner: LifecycleOwner? = null, service: Class<S>) : this(
        context,
        owner
    ) {
        serviceClazz = service
    }

    constructor(activity: ComponentActivity, action: String) : this(activity, activity, action)
    constructor(context: Context, owner: LifecycleOwner? = null, action: String) : this(
        context,
        owner
    ) {
        intent = Intent(action)
    }

    companion object {

        fun <S : Service> newBinder(service: S) = SimpleServiceBinder(service)
    }


    private var service: S? = null
    private var conn: ServiceConnection? = null
    private var binder: SimpleServiceBinder<S>? = null
    private var serviceClazz: Class<S>? = null
    private var intent: Intent? = null
    var isBind = false

    init {
        obWhenCreate(owner, onCreate = { bind() }, onDestroy = { unbind() })
    }

    /**
     * 用于处理Helper对象创建在context能使用之前的情形
     */
    private fun checkIntent() {
        if (intent == null && serviceClazz != null) {
            intent = Intent(context, serviceClazz)
        } else {
            throw Exception("error")
        }
    }

    fun unbind() {
        if (conn != null) {
            context.unbindService(conn!!)
        }
        binder?.clear()
        binder = null
        service = null
        conn = null
    }

    fun bind(): ServiceBindHelper<S> {
        if (conn != null) {
            return this
        }
        checkIntent()
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
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isBind = false
            }

        }
        context.bindService(intent, conn!!, Service.BIND_AUTO_CREATE)
        return this
    }

    fun opService(func: (S) -> Unit) {
        service?.let {
            func(it)
        }
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