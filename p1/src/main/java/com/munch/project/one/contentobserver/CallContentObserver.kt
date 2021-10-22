package com.munch.project.one.contentobserver

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.CallLog
import android.provider.Telephony
import com.munch.lib.app.AppHelper
import com.munch.lib.log.log

/**
 * Create by munch1182 on 2021/10/22 09:32.
 */
//电话记录的更改只在电话操作后（通话结束、拒绝接听或者未接听）
//@RequiresPermission("Manifest.permission.READ_CALL_LOG")
class CallContentObserver(handler: Handler) : ContentObserver(handler) {

    private var lastRecordId = -1

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        log("onChange:$uri")
        when {
            uri == CallLog.Calls.CONTENT_URI -> queryLastCall()
            //sms的uri是变化的，包括短信通知和已读短信通知
            uri?.toString()?.contains("sms") == true -> querySms()
        }
    }

    private fun querySms() {
        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            null,
            null,
            Telephony.Sms.DEFAULT_SORT_ORDER
        )?.apply {
            if (!moveToFirst()) {
                return@apply
            }
            log("sms count:${this.columnCount}")
            val id = getInt(getColumnIndex("_id"))
            //通话类型，1 来电 .INCOMING_TYPE；2 已拨 .OUTGOING_；3 未接 .MISSED_
            val type = getInt(getColumnIndex(Telephony.Sms.TYPE))
            val address = getString(getColumnIndex(Telephony.Sms.ADDRESS))
            val date = getInt(getColumnIndex(Telephony.Sms.DATE))
            val isRead = getInt(getColumnIndex(Telephony.Sms.READ))
            val body = getString(getColumnIndex(Telephony.Sms.BODY))
            log("sms:id:$id,type:$type,address:$address,date:$date,isRead:$isRead,body:$body")
        }?.close()
    }

    private val context: Context
        get() = AppHelper.app

    private fun queryLastCall() {
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
            log("call count:${this.columnCount}")
            val id = getInt(getColumnIndex("_id"))
            //通话类型，1 来电 .INCOMING_TYPE；2 已拨 .OUTGOING_；3 未接 .MISSED_
            val type = getInt(getColumnIndex(CallLog.Calls.TYPE))
            val number = getString(getColumnIndex(CallLog.Calls.NUMBER))
            val duration = getInt(getColumnIndex(CallLog.Calls.DURATION))
            //只有未接来电(type3)此值才会变化，(type1)此值不会变化
            //0未读，1已读，此值可看作删除了记录
            val isRead = getInt(getColumnIndex(CallLog.Calls.IS_READ))
            log("call:id:$id,type:$type,number:$number,duration:$duration,isRead:$isRead")
        }?.close()
    }
}