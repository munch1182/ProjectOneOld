package com.munch.project.one.load.net

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * 将某个请求实际返回的数据，通过该转换方法转为定义的数据
 * 比如：将统一有外包裹类的数据请求，返回时只返回包裹内的数据
 *
 * 如:
 * 一般返回: NetResult(val errorCode:Int, val data:T?)
 *      @GET
 *      fun query():NetResult<Data>
 *
 * 此类实现:
 *      @GET
 *      @NetDataTransformer
 *      fun query():Data
 *
 * Create by munch1182 on 2021/9/16 17:40.
 */

/**
 * @param key 用于标记该转换，可以在[NetResNoWrapperCallAdapterFactory]中根据该标记区分并实现转换的具体方法
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NetDataTransformer(val key: String = "")

class NetResNoWrapperCallAdapterFactory(private val transformer: ((key: String, origin: ResponseBody) -> ResponseBody)?) :
    Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        /*transformer ?: return null
        return annotations.filterIsInstance<NetDataTransformer>()
            .firstOrNull()?.key.let { key ->
                //交由下一个能处理该返回类型的AdapterFactory处理结果值
                retrofit.nextResponseBodyConverter<Any>(this, type, annotations)
                    ?.let { converter -> NetConverter<Any>(key, transformer, converter) }
            }*/
        return null
    }
}

private class NetConverter<T> : Converter<ResponseBody, T?> {
    override fun convert(value: ResponseBody): T? {
        TODO("Not yet implemented")
    }

}