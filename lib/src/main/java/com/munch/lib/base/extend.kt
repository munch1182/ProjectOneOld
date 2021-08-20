package com.munch.lib.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Create by munch1182 on 2021/8/19 15:05.
 */

fun <T> MutableLiveData<T>.toLive(): LiveData<T> = this
