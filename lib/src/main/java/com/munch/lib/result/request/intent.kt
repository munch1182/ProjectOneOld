package com.munch.lib.result.request

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.munch.lib.Resettable
import com.munch.lib.log.Logger
import com.munch.lib.result.OnIntentResultListener

/**
 * Created by munch1182 on 2022/4/10 4:18.
 */
interface IntentRequest {
    fun startIntent(intent: Intent, listener: OnIntentResultListener?)
}

class IntentRequestHandler(fragment: Fragment) : IntentRequest, Resettable,
    ActivityResultCaller by fragment {

    private val log = Logger("intent")
    private var listener: OnIntentResultListener? = null
    private val onResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val isOk = it.resultCode == Activity.RESULT_OK
            log.log("receive intent: $isOk.")
            listener?.onIntentResult(isOk, it.resultCode, it.data)
            reset()
        }

    override fun startIntent(intent: Intent, listener: OnIntentResultListener?) {
        reset()
        this.listener = listener
        log.log("launch intent.")
        onResultLauncher.launch(intent)
    }

    override fun reset() {
        listener = null
    }
}