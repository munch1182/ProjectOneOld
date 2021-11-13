package com.munch.project.one.data

import com.munch.lib.fast.base.DataHelper
import com.munch.project.one.broadcast.LogActionVB

/**
 * Create by munch1182 on 2021/9/14 15:35.
 */
object DataHelper {


    object LogReceive {

        private const val ACTIONS = "actions"
        private const val ACTIONS_IS_CHECKED = "actions_is_checked"

        fun saveActions(actions: List<LogActionVB>) {
            val byteArray = ByteArray(actions.size)
            val sb = StringBuilder()
            actions.forEachIndexed { i, b ->
                if (i > 0) {
                    sb.append(",")
                }
                sb.append(b.action)
                byteArray[i] = if (b.isCheck) 1.toByte() else 0.toByte()
            }
            DataHelper.App.instance.apply {
                put(ACTIONS, sb.toString())
                put(ACTIONS_IS_CHECKED, byteArray)
            }
        }

        fun getActions(): List<LogActionVB> {
            val instance = DataHelper.App.instance
            if (!instance.hasKey(ACTIONS)) {
                return emptyList()
            }
            val actionsStr = instance.get(ACTIONS, "")
            if (actionsStr.isNullOrEmpty()) {
                return emptyList()
            }
            val actions = actionsStr.split(",")
            val isCheck = instance.get(ACTIONS_IS_CHECKED, byteArrayOf()) ?: return emptyList()
            if (isCheck.size != actions.size) {
                return emptyList()
            }
            return List(isCheck.size) { LogActionVB(actions[it], isCheck[it] == 1.toByte()) }
        }
    }

}