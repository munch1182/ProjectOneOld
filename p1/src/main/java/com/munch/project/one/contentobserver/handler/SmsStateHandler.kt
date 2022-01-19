package com.munch.project.one.contentobserver.handler

import android.Manifest
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.Telephony
import com.munch.lib.base.PermissionHandler
import com.munch.lib.log.Logger


/**
 * Create by munch1182 on 2022/1/19 15:47.
 */
open class SmsStateHandler(
    override val context: Context,
    private val handler: Handler,
    private var listener: OnSmsReceiveListener? = null,
    val logger: Logger? = null
) : PermissionHandler, IObserver {

    override val permissions: Array<String>
        get() = arrayOf(Manifest.permission.READ_SMS)

    override fun onPermissionGranted() {
        super.onPermissionGranted()
        startObserve()
    }

    private val smsContent by lazy {
        object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                logger?.log("onChange: $uri")
                try {
                    context.contentResolver.query(
                        Telephony.Sms.CONTENT_URI, null, null, null,
                        Telephony.Sms.DEFAULT_SORT_ORDER
                    )?.apply {
                        //因为只有调用了一次，所以多条短信同时阅读无法触及
                        if (!moveToFirst()) {
                            return@apply
                        }
                        try {
                            val type = getInt(getColumnIndexOrThrow(Telephony.Sms.TYPE))

                            val id = getInt(getColumnIndexOrThrow(Telephony.Sms._ID))
                            val address = getString(getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                            val date = getInt(getColumnIndexOrThrow(Telephony.Sms.DATE))
                            val isRead = getInt(getColumnIndexOrThrow(Telephony.Sms.READ))
                            val body = getString(getColumnIndexOrThrow(Telephony.Sms.BODY))
                            logger?.log("sms:id:$id,type:$type,number:$address,date:$date,isRead:$isRead")
                            if (type != Telephony.Sms.MESSAGE_TYPE_INBOX) {
                                return@apply
                            }
                            if (isRead == 1) {
                                listener?.onSmsRead(id)
                            } else {
                                listener?.onSmsReceived(id, address, body)
                            }
                        } catch (e: Exception) {
                            logger?.log("fail to query sms content")
                            logger?.log(e)
                        }
                    }?.close()
                } catch (e: Exception) {
                    logger?.log("fail to query sms")
                    logger?.log(e)
                }
            }
        }
    }

    override fun startObserve() {
        try {
            logger?.log("start listen sms content change")
            context.contentResolver.registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                smsContent
            )
        } catch (e: Exception) {
            logger?.log("fail to start listen sms content change")
            logger?.log(e)
        }
    }

    override fun stopObserve() {
        try {
            logger?.log("stop listen sms content change")
            context.contentResolver.unregisterContentObserver(smsContent)
        } catch (e: Exception) {
            logger?.log("fail to stop listen sms content change")
            logger?.log(e)
        }
    }

    interface OnSmsReceiveListener {

        fun onSmsReceived(id: Int, number: String, content: String)

        fun onSmsRead(id: Int)
    }
}