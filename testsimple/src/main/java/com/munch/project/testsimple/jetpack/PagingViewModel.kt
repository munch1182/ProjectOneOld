package com.munch.project.testsimple.jetpack

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel

/**
 * Create by munch1182 on 2020/12/17 15:40.
 */
class PagingViewModel @ViewModelInject constructor(var repository: Repository) :
    ViewModel() {

    val allUser = repository.queryUser()

}