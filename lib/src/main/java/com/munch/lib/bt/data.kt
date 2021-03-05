package com.munch.lib.bt

/**
 * Create by munch1182 on 2021/3/5 14:46.
 */
sealed class BtData {

    open fun write(byteArray: ByteArray) {}

    open fun read(): ByteArray = byteArrayOf()

}