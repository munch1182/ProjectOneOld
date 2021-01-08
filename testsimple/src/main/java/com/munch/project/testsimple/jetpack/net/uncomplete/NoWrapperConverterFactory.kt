package com.munch.project.testsimple.jetpack.net.uncomplete

import com.munch.lib.UNCOMPLETE
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Create by munch1182 on 2020/12/19 11:45.
 */
@UNCOMPLETE
class NoWrapperConverterFactory : Converter.Factory() {

    companion object {

        fun create() = NoWrapperConverterFactory()
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, FlowNoWrapper<Any>>? {
        if (type != FlowNoWrapper::class.java) {
            return null
        }
        return FlowNoWrapperConverter()
    }

    class FlowNoWrapperConverter : Converter<ResponseBody, FlowNoWrapper<Any>> {
        override fun convert(value: ResponseBody): FlowNoWrapper<Any> {
            /*val toString = value.toString()*/

            return FlowNoWrapper {
                /*emit(
                    Gson().fromJson<BaseDtoWrapper<Any>>(
                        value.string(),
                        BaseDtoWrapper::class.java
                    )
                )*/
            }
        }
    }
}