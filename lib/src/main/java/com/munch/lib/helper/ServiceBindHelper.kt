package com.munch.lib.helper

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.munch.lib.helper.ServiceBindHelper.Companion.newBinder

/**
 * service通用绑定，需要[Service.onBind]返回[newBinder]
 *
 * @param owner 用于自动绑定和解绑，如为null，需要手动调用[bind]和[unbind]，
 * 用bind方式的service在bind组件生命周期外必须解绑，否则会内存泄漏
 *
 * Create by munch1182 on 2020/12/28 10:06.
 */
class ServiceBindHelper<S : Service>
private constructor(
    private val context: Context,
    private val owner: LifecycleOwner?,
) {

    private var serviceClazz: Class<S>? = null
    private var intent: Intent? = null

    constructor(context: Context, owner: LifecycleOwner?, intent: Intent) : this(
        context,
        owner
    ) {
        this.intent = intent
    }

    constructor(context: Context, owner: LifecycleOwner?, service: Class<S>) : this(
        context,
        owner
    ) {
        serviceClazz = service
    }

    constructor(context: Context, owner: LifecycleOwner?, action: String) : this(
        context,
        owner
    ) {
        intent = Intent(action)
    }

    companion object {

        fun <S : Service> newBinder(service: S) = SimpleServiceBinder(service)
    }

    init {
        owner?.lifecycle?.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                bind()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                unbind()
                owner.lifecycle.removeObserver(this)
            }
        })
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
        service = null
        conn = null
    }

    fun bind(): ServiceBindHelper<S> {
        checkIntent()
        conn = object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: android.os.IBinder?
            ) {
                @Suppress("UNCHECKED_CAST")
                if (service is SimpleServiceBinder<*>) {
                    this@ServiceBindHelper.service = (service as SimpleServiceBinder<S>).get()
                    isBind = true
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isBind = false
                conn = null
                service = null
            }

        }
        context.bindService(intent, conn!!, Service.BIND_AUTO_CREATE)
        return this
    }

    private var service: S? = null
    private var conn: ServiceConnection? = null
    var isBind = false

    fun opService(func: (S) -> Unit) {
        service?.let {
            func(it)
        }
    }

    /**
     * 服务的调用必须在确保已经绑定之后
     */
    fun getService(): S? = service

    class SimpleServiceBinder<T : Service> constructor(private val service: T) : Binder() {

        fun get(): T {
            return service
        }
    }
}