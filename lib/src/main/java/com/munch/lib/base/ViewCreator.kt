package com.munch.lib.base

import android.content.Context
import android.view.View

@FunctionalInterface
interface ViewCreator {

    fun create(context: Context): View
}