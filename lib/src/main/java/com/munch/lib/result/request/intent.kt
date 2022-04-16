package com.munch.lib.result.request

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.munch.lib.base.Resettable
import com.munch.lib.result.OnIntentResultListener

/**
 * Created by munch1182 on 2022/4/10 4:18.
 */
interface IntentRequest {
    fun startIntent(intent: Intent, listener: OnIntentResultListener?)
}

class IntentRequestHandler(fragment: Fragment) : IntentRequest, Resettable,
    ActivityResultCaller by fragment {

    private var listener: OnIntentResultListener? = null
    private val onResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            listener?.onIntentResult(it.resultCode == Activity.RESULT_OK, it.resultCode, it.data)
            reset()
        }

    override fun startIntent(intent: Intent, listener: OnIntentResultListener?) {
        this.listener = listener
        onResultLauncher.launch(intent)
    }

    override fun reset() {
        listener = null
    }
}