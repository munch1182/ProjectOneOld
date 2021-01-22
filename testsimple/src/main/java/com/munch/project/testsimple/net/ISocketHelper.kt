package com.munch.project.testsimple.net

/**
 * Create by munch1182 on 2021/1/22 9:35.
 */
interface ISocketHelper {

    fun startSocketService()

    fun stopSocketService()

    fun clientConnect()

    fun clientSend(msg: String)

    fun clientDisconnect()

    fun closeResource()
}