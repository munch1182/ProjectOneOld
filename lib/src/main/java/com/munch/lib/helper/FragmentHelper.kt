package com.munch.lib.helper

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

/**
 * Create by munch1182 on 2021/11/14 1:49.
 */
class FragmentHelper(
    private val fm: FragmentManager,
    private val id: Int = 0,
    private val tag: String? = null
) {

    companion object {
        fun FragmentManager.replace(id: Int, fragment: Fragment, stack: Boolean = false) {
            beginTransaction().apply {
                replace(id, fragment)
                if (stack) {
                    addToBackStack(null)
                }
            }.commit()
        }
    }

    constructor(
        activity: FragmentActivity, id: Int = 0,
        tag: String? = null
    ) : this(activity.supportFragmentManager, id, tag)

    constructor(
        fragment: Fragment, id: Int = 0,
        tag: String? = null
    ) : this(fragment.childFragmentManager, id, tag)

    private val fragments = arrayListOf<Fragment>()

    fun show(fragment: Fragment) {
        fm.beginTransaction()
            .apply {
                fragments.lastOrNull()?.let {
                    hide(it)
                }
                if (!fragments.contains(fragment)) {
                    add(id, fragment, tag)
                } else {
                    fragments.remove(fragment)
                }
                fragments.add(fragment)
                show(fragment)
            }.commit()
    }

    fun remove(fragment: Fragment) {
        if (!fragments.contains(fragment)) {
            return
        }
        fm.beginTransaction().apply {
            fragments.remove(fragment)
            remove(fragment)
        }.commit()
    }

    fun pop(): Boolean {
        val willHandle = fragments.size > 1
        if (willHandle) {
            val fragment = fragments.lastOrNull() ?: return false
            fm.beginTransaction().apply {
                fragments.removeAt(fragments.lastIndex)
                remove(fragment)
                fragments.lastOrNull()?.let {
                    show(it)
                }
            }.commit()
        }
        return willHandle
    }

}