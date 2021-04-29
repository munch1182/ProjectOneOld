package com.munch.pre.lib.bluetooth

import com.munch.pre.lib.helper.format

/**
 * Create by munch1182 on 2021/4/28 13:52.
 */
data class SendPack(
    val bytes: ByteArray,
    // 每次发送等待时间
    val timeout: Long = 3000L,
    // 重试次数
    val retryCount: Int = 3,
    val needReceived: Boolean = true,
    //接收回调
    val listener: OnReceivedCheckedListener? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SendPack

        if (!bytes.contentEquals(other.bytes)) return false
        if (timeout != other.timeout) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + timeout.hashCode()
        return result
    }

    override fun toString(): String {
        return "SendPack(${bytes.format()}, timeout=$timeout, needReceived=$needReceived, listener=${listener})"
    }
}