package com.munch.lib.base

import android.os.Looper
import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Create by munch1182 on 2020/12/7 10:45.
 */
open class BaseRootFragment : Fragment() {

    fun toast(msg: String) {
        when {
            activity is BaseRootActivity -> {
                (activity as BaseRootActivity).toast(msg)
            }
            Looper.getMainLooper().thread == Thread.currentThread() -> {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            else -> {
                activity?.runOnUiThread {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

}