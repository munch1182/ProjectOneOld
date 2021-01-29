package com.munch.project.testsimple.net

import com.munch.lib.helper.formatDate

data class SocketContentBean(
    val content: String,
    val ip: String,
    val port: Int,
    val time: Long = System.currentTimeMillis()
) {

    fun name(): String {
        return ip
    }

    fun showName(): Boolean {
        return ip.isNotEmpty()
    }

    fun timeStr(): String {
        return "yyyy-MM-dd HH:mm:ss".formatDate(time)
    }
}