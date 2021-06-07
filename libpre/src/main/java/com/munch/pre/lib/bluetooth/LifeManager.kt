package com.munch.pre.lib.bluetooth

import com.munch.pre.lib.base.Cancelable
import com.munch.pre.lib.base.Destroyable

internal class LifeManager {
    private val manageable = mutableListOf<Manageable>()

    fun manage(vararg manageable: Manageable) {
        manageable.forEach {
            this.manageable.add(it)
        }
    }

    fun cancel() {
        manageable.forEach { it.cancel() }
    }

    fun destroy() {
        manageable.forEach { it.destroy() }
        manageable.clear()
    }

}

interface Manageable : Cancelable, Destroyable
