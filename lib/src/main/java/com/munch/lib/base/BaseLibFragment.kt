package com.munch.lib.base

import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Create by munch1182 on 2020/12/7 10:45.
 */
open class BaseLibFragment : Fragment() {

    fun toast(msg: String) {
        activity?.runOnUiThread {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

}