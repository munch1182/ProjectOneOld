package com.munch.lib.fast.base

import android.os.Build.*

/**
 * Created by munch1182 on 2022/4/30 20:26.
 */
object PhoneHelper {
    //系统定制商: meizu
    val brand: String
        get() = BRAND

    //手机型号: 16X
    val model: String
        get() = MODEL

    //版本: 10
    val version: String
        get() = VERSION.RELEASE

    //版本: 29
    val versionCode: String
        get() = VERSION.SDK_INT.toString()

    //显示的版本: Flyme 9.21.11.18 bate
    val romVersion: String
        get() = DISPLAY

    //产品型号: meizu_16X_CN
    val product: String
        get() = PRODUCT

    //生产ID: QKQ1.191222.002
    val id: String
        get() = ID

    //主机: Mz-Builder-I7
    val host: String
        get() = HOST

    //硬件类型: qcom
    val hardware: String
        get() = HARDWARE

    //开发代号: REL
    val condeName: String
        get() = VERSION.CODENAME

    //rom制造商: Meizu
    val rom: String
        get() = MANUFACTURER

    fun collect(): Map<String, String> = linkedMapOf(
        "brand" to brand,
        "model" to model,
        "version" to version,
        "versionCode" to versionCode,
        "rom" to rom,
        "romVersion" to romVersion,
        "product" to product,
        "id" to id,
        "host" to host,
        "hardware" to hardware,
        "codeName" to condeName,
    )

    fun desc() = "$brand $model $romVersion android$version version$versionCode"
}