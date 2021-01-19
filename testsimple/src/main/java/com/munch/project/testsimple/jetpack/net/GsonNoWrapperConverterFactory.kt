package com.munch.project.testsimple.jetpack.net

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonToken
import com.munch.lib.log
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.Writer
import java.lang.reflect.Type
import java.nio.charset.Charset

/**
 * Create by munch1182 on 2021/1/19 17:05.
 */
@Deprecated("复制而来，未作改动")
class GsonNoWrapperConverterFactory(private val gson: Gson) : Converter.Factory() {

    /*companion object {

        fun create(gson: Gson): GsonNoWrapperConverterFactory {
            return GsonNoWrapperConverterFactory(gson)
        }
    }*/

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        log(parameterAnnotations, methodAnnotations, type)
        return GsonRequestBodyConverter(gson, gson.getAdapter(TypeToken.get(type)))
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        log(TypeToken.get(type))
        return GsonResponseBodyConverter(gson, gson.getAdapter(TypeToken.get(type)))
    }
}

internal class GsonResponseBodyConverter<T>(
    private val gson: Gson,
    private val adapter: TypeAdapter<T>
) : Converter<ResponseBody, T> {
    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T {
        val jsonReader = gson.newJsonReader(value.charStream())
        return value.use {
            val result = adapter.read(jsonReader)
            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw JsonIOException("JSON document was not fully consumed.")
            }
            result
        }
    }
}

internal class GsonRequestBodyConverter<T>(
    private val gson: Gson,
    private val adapter: TypeAdapter<T>
) :
    Converter<T, RequestBody> {

    @Throws(IOException::class)
    override fun convert(value: T): RequestBody {
        val buffer = Buffer()
        val writer: Writer = OutputStreamWriter(buffer.outputStream(), UTF_8)
        val jsonWriter = gson.newJsonWriter(writer)
        adapter.write(jsonWriter, value)
        writer.close()
        jsonWriter.close()
        return buffer.readByteString().toRequestBody(MEDIA_TYPE)
    }

    companion object {
        private val MEDIA_TYPE: MediaType = "application/json; charset=UTF-8".toMediaType()
        private val UTF_8 = Charset.forName("UTF-8")
    }
}