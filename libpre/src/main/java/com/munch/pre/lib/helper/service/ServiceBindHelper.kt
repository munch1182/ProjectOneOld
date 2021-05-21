package com.munch.pre.lib.helper.service

import android.app.Application
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ComponentActivity
import java.io.Closeable

/**
 * 服务绑定帮助类
 *
 * 主要使不用手动创建和维护ServiceConnection对象
 *
 * Create by munch1182 on 2021/4/25 15:14.
 */
class ServiceBindHelper<S : Service>(private val intent: Intent) {

    companion object {

        fun <S : Service> newBinder(service: S) = SimpleBinder(service)

        inline fun <reified S : Service> bindActivity(activity: ComponentActivity): ServiceBindHelper<S> {
            return ServiceBindHelper(Intent(activity, S::class.java))
        }

        inline fun <reified S : Service> bindApp(app: Application) =
            ServiceBindHelper<S>(Intent(app, S::class.java))

        fun <S : Service> bindService(action: String) =
            ServiceBindHelper<S>(Intent(action))
    }

    private var binder: SimpleBinder<S>? = null
    private var isBound = false
    var onBind: ((s: S?) -> Unit)? = null
    private var conn: ServiceConnection? = null
    private fun getConnOrNew(): ServiceConnection {
        if (conn == null) {
            conn = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    @Suppress("UNCHECKED_CAST")
                    if (service is SimpleBinder<*>) {
                        binder = service as SimpleBinder<S>
                        isBound = true
                        onBind?.invoke(binder!!.get())
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    isBound = false
                }
            }
        }
        return conn!!
    }

    fun bind(context: Context) {
        if (isBound) {
            return
        }
        context.bindService(intent, getConnOrNew(), Service.BIND_AUTO_CREATE)
    }

    fun unbind(context: Context) {
        if (isBound && conn != null) {
            context.unbindService(conn!!)
        }
        isBound = false
        binder?.close()
        binder = null
    }

    fun isBind() = isBound

    open class SimpleBinder<S : Service> constructor(private var service: S?) : Binder(),
        Closeable {

        fun get(): S? = service

        override fun close() {
            service = null
        }
    }
}