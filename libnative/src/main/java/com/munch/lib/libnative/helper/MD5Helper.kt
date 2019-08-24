package com.munch.lib.libnative.helper

import androidx.annotation.NonNull
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


/**
 * Created by Munch on 2019/8/20 17:56
 */
object MD5Helper {
    /**
     * 32位MD5加密
     * @param content -- 待加密内容
     * @return
     */
    @JvmStatic
    fun md5Decode(content: String): String {
        val md5: String
        val b: ByteArray?
        try {
            val md = MessageDigest.getInstance("MD5")
            md.update(content.toByteArray())

            b = md.digest()

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }

        var i: Int
        val buf = StringBuffer("")
        for (offset in b.indices) {
            i = b[offset].toInt()
            if (i < 0)
                i += 256
            if (i < 16)
                buf.append("0")
            buf.append(Integer.toHexString(i))
        }

        md5 = buf.toString()

        return md5
    }

    @JvmStatic
    @NonNull
    fun md5DecodeOrReturn(content: String): String {
        return try {
            md5Decode(content)
        } catch (e: Exception) {
            e.printStackTrace()
            content
        }
    }
}