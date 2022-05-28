package com.munch.lib.result

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Created by munch1182 on 2022/5/28 5:44.
 */
class ContactRequest(private val fragment: ResultFragment) : Iterable<ResultHelper.IRequest> {

    private val list = mutableListOf<ResultHelper.IRequest>()

    fun contact(vararg permission: String): ContactRequest {
        list.add(PermissionRequest(permission, fragment))
        return this
    }

    fun contact(intent: Intent): ContactRequest {
        list.add(IntentRequest(intent, fragment))
        return this
    }

    fun contact(judge: OnJudge, intent: OnIntent): ContactRequest {
        list.add(JudgeOrIntentRequest(judge, intent.invoke(fragment.requireContext()), fragment))
        return this
    }

    private var explain: ExplainContactNotice? = null

    fun explain(explain: (Context) -> ExplainContactNotice): ContactRequest {
        this.explain = explain.invoke(fragment.requireContext())
        return this
    }

    fun start(onResult: (isOk: Boolean) -> Unit) {
        fragment.launch(Dispatchers.Default) {
            var result = true
            kotlin.run stop@{
                forEach { req ->
                    when (req) {
                        is IntentRequest -> result = suspendCancellableCoroutine {
                            explain?.let { e -> req.explain { e } }
                            req.start { isOk, _ -> it.resume(isOk) }
                        }
                        is JudgeOrIntentRequest -> result = suspendCancellableCoroutine {
                            explain?.let { e -> req.explain { e } }
                            req.start { isOk -> it.resume(isOk) }
                        }
                        is PermissionRequest -> result = suspendCancellableCoroutine {
                            explain?.let { e -> req.explain { e } }
                            req.request { isGrantAll, _ -> it.resume(isGrantAll) }
                        }
                    }
                    if (!result) {
                        return@stop
                    }
                }
            }
            onResult.invoke(result)
        }
    }

    override fun iterator() = RequestIterator()

    inner class RequestIterator : Iterator<ResultHelper.IRequest> {
        override fun hasNext() = list.isNotEmpty()

        override fun next() = list.removeFirst()
    }
}

interface ExplainContactNotice : ExplainPermissionNotice, ExplainIntentNotice