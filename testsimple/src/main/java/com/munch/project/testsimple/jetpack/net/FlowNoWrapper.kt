package com.munch.project.testsimple.jetpack.net

import kotlinx.coroutines.flow.Flow

/**
 * Create by munch1182 on 2020/12/18 16:16.
 */
interface FlowNoWrapper<T> : Flow<T>