@file:Suppress("DEPRECATION")
package com.munch.project.one.contentobserver.handler

import android.Manifest
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.munch.lib.base.PermissionHandler
import com.munch.lib.log.Logger

/**
 *
 * 用于处理来电状态的监听和相关权限的判断
 * Create by munch1182 on 2022/1/19 11:20.
 */
open class CallStateHandler(
    override val context: Context,
    private val handler: Handler,
    private var listener: OnCallListener? = null,
    private var logger: Logger? = null
) : PermissionHandler, IObserver {

    fun setListener(listener: OnCallListener?) {
        this.listener = listener
    }

    override val permissions: Array<String>
        get() = arrayOf(
            Manifest.permission.READ_CALL_LOG
        )

    private val tm by lazy { context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager? }

    private val phoneStateListener = object : PhoneStateListener() {
        /**
         * 用以区分IDLE的多次触发
         */
        private var isCallHanding = false

        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            logger?.log("onCallStateChanged: state=$state, phoneNumber=$phoneNumber")
            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                    if (!isCallHanding) {
                        return
                    }
                    isCallHanding = false
                    //主动挂断或者被挂断:未接听
                    logger?.log("callState: ring over")
                    listener?.onRefuse()
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    if (!isCallHanding) {
                        return
                    }
                    isCallHanding = false
                    logger?.log("callState: answer")
                    listener?.onAnswer()
                }
                TelephonyManager.CALL_STATE_RINGING -> {
                    isCallHanding = true
                    logger?.log("callState: ringing(number=$phoneNumber)")
                    listener?.onCall(phoneNumber)
                }
            }
        }
    }

    private val callContent by lazy {
        object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                logger?.log("call content change: uri=$uri")
                if (uri == CallLog.Calls.CONTENT_URI) {
                    try {
                        context.contentResolver.query(
                            CallLog.Calls.CONTENT_URI,
                            null,
                            null,
                            null,
                            CallLog.Calls.DEFAULT_SORT_ORDER
                        )?.apply {
                            if (!moveToFirst()) {
                                return@apply
                            }
                            try {
                                val id = getInt(getColumnIndexOrThrow("_id"))
                                //通话类型，1 来电 .INCOMING_TYPE；2 已拨 .OUTGOING_；3 未接 .MISSED_
                                val type = getInt(getColumnIndexOrThrow(CallLog.Calls.TYPE))
                                val number = getString(getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                                val duration = getInt(getColumnIndexOrThrow(CallLog.Calls.DURATION))
                                //只有未接来电(type3)此值才会变化，(type1)此值不会变化
                                //0未读，1已读，此值可看作删除了记录
                                val isRead = getInt(getColumnIndexOrThrow(CallLog.Calls.IS_READ))
                                logger?.log("call:id:$id,type:$type,number:$number,duration:$duration,isRead:$isRead")
                                if (type == CallLog.Calls.MISSED_TYPE) {
                                    listener?.onMissPhone(number, isRead == 1)
                                }
                            } catch (e: Exception) {
                                logger?.log("fail to query call content")
                                logger?.log(e)
                            }
                        }?.close()
                    } catch (e: Exception) {
                        logger?.log("fail to query call")
                        logger?.log(e)
                    }
                }
            }
        }
    }

    override fun onPermissionGranted() {
        super.onPermissionGranted()
        startObserve()
    }

    override fun startObserve() {
        try {
            //此方法虽然在android12中废弃，但仍然可用
            //且android12的替代方法只提供了来电状态，未提供来电号码
            logger?.log("start listen phone state")
            tm?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        } catch (e: Exception) {
            logger?.log("fail to listener phone state")
            logger?.log(e)
        }
        try {
            logger?.log("start listen call content change")
            context.contentResolver.registerContentObserver(
                CallLog.Calls.CONTENT_URI, true, callContent
            )
        } catch (e: Exception) {
            logger?.log("fail to listen call content change")
            logger?.log(e)
        }
    }

    override fun stopObserve() {
        try {
            logger?.log("stop listen phone state")
            tm?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        } catch (e: Exception) {
            logger?.log("fail to stop listen phone state")
            logger?.log(e)
        }
        try {
            logger?.log("stop listen call content change")
            context.contentResolver.unregisterContentObserver(callContent)
        } catch (e: Exception) {
            logger?.log("fail to stop listen call content change")
            logger?.log(e)
        }
    }


    interface OnCallListener {

        /**
         * 来电
         */
        fun onCall(number: String?)

        /**
         * 接听
         */
        fun onAnswer()

        /**
         * 主动挂断
         */
        fun onRefuse()

        /**
         * 未接来电
         */
        fun onMissPhone(number: String?, read: Boolean)
    }
}