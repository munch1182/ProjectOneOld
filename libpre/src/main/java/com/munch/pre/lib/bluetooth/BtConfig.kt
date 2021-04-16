package com.munch.pre.lib.bluetooth

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Create by munch1182 on 2021/4/8 10:56.
 */
@Parcelize
open class BtConfig(
    var UUID_MAIN_SERVER: String? = null,
    var UUID_WRITE: String? = null,
    var UUID_NOTIFY: String? = null,
    var UUID_DESCRIPTOR_NOTIFY: String? = null,
    var MTU_VALUE: Int? = MAX_MTU
) : Parcelable {

    companion object {
        const val MAX_MTU = 247
    }

    override fun toString(): String {
        return "BtConfig(UUID_MAIN_SERVER=$UUID_MAIN_SERVER, UUID_WRITE=$UUID_WRITE, UUID_NOTIFY=$UUID_NOTIFY, UUID_DESCRIPTOR_NOTIFY=$UUID_DESCRIPTOR_NOTIFY, MTU_MAX=$MTU_VALUE)"
    }
}