package com.munch.project.one.load.net

import com.munch.lib.base.newInstance
import com.munch.lib.base.toClass
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Create by munch1182 on 2021/9/19 15:49.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NetDataTransformer(
    val type: KClass<*> = Void::class,
    val former: KClass<out NetTransformer<*, *>> = NetNonFormer::class
)

interface NetTransformer<FROM, TO> {

    fun transform(from: FROM): TO
}

object NetNonFormer : NetTransformer<Any, Any> {
    override fun transform(from: Any): Any {
        return from
    }
}

class NetTransformCallAdapterFactory(
    private val type: Class<*>? = null,
    private val former: NetTransformer<*, *>? = null
) : CallAdapter.Factory() {

    companion object {

        fun create(type: Class<*>? = null, former: NetTransformer<*, *>? = null) =
            NetTransformCallAdapterFactory(type, former)
    }

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        //如果没有使用注解NetNoWrapper则返回
        val annotation =
            annotations.filterIsInstance<NetDataTransformer>().firstOrNull() ?: return null
        val realType = annotation.type
            .takeIf { it != Void::class }
            ?.toClass() ?: type ?: return null
        val realFormer = annotation.former
            .takeIf { it != NetNonFormer::class }
            ?.newInstance() ?: former ?: return null

        //如果使用了suspend则返回值类型为Call，否则不处理
        if (getRawType(returnType) != Call::class.java) {
            return null
        }
        //如果没有数据类型，则不处理
        if (returnType !is ParameterizedType) {
            return null
        }
        //获取数据类型
        val responseType = getParameterUpperBound(0, returnType)


        //如果使用了注解转换该数据类型，但返回值仍然是该类型，则注解无效

        if (responseType == realType) {
            return null
        }

        @Suppress("UNCHECKED_CAST")
        return NetNoWrapperCallAdapter(
            object : ParameterizedType {
                override fun getActualTypeArguments() = arrayOf(responseType)

                override fun getRawType() = realType

                override fun getOwnerType(): Type? = null
            }, realFormer as NetTransformer<Any, Any>
        )
    }
}

private class NetNoWrapperCallAdapter<D, T>(
    private val responseType: Type, private val former: NetTransformer<D, T>
) : CallAdapter<D, Call<T?>> {
    override fun responseType(): Type {
        return responseType
    }

    override fun adapt(call: Call<D>): Call<T?> {
        return NetConvertCall(call, former)
    }

}

private class NetConvertCall<D, T>(
    private val call: Call<D>,
    private val former: NetTransformer<D, T>
) : Call<T?> {

    override fun clone() = NetConvertCall(call, former)

    override fun execute(): Response<T?> {
        val response = call.execute()
        return convertBody(response)
    }

    private fun convertBody(response: Response<D>) = if (response.isSuccessful) {
        val body = response.body()
        if (body == null) {
            Response.error(500, "Empty ResponseBody".toResponseBody())
        } else {
            Response.success(former.transform(body))
        }
    } else {
        Response.error(response.code(), response.errorBody() ?: "no error body".toResponseBody())
    }

    override fun enqueue(callback: Callback<T?>) {
        call.enqueue(object : Callback<D> {
            override fun onResponse(call: Call<D>, response: Response<D>) {
                callback.onResponse(this@NetConvertCall, convertBody(response))
            }

            override fun onFailure(call: Call<D>, t: Throwable) {
                callback.onFailure(this@NetConvertCall, t)
            }

        })
    }

    override fun isExecuted() = call.isExecuted

    override fun cancel() = call.cancel()

    override fun isCanceled() = call.isCanceled

    override fun request(): Request = call.request()

    override fun timeout(): Timeout = call.timeout()

}