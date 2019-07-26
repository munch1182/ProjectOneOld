package com.munhc.lib.libnative.excetion

/**
 * Created by Munch on 2019/7/26 14:30
 */
class MethodEcxception(message: String?) : BException(false, message) {


    companion object {

        fun unSupport(): MethodEcxception {
            return MethodEcxception("不支持这样调用")
        }

        fun wrongParameter(): MethodEcxception {
            return MethodEcxception("参数错误")
        }
    }
}