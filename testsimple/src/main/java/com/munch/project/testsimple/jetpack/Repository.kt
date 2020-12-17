package com.munch.project.testsimple.jetpack

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.munch.project.testsimple.jetpack.data.User
import com.munch.project.testsimple.jetpack.data.UserDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Create by munch1182 on 2020/12/17 15:52.
 */
class Repository @Inject constructor(private var userDao: UserDao) {

    fun queryUser(): Flow<PagingData<User>> {
        val config = PagingConfig(
            pageSize = 15,
            prefetchDistance = 4,
            initialLoadSize = 30,
            maxSize = 45
        )
        return Pager(
            config, pagingSourceFactory =
            userDao.queryAllData().asPagingSourceFactory()
        ).flow
    }

    fun insertUser() {
        User.newArrayInstance().forEach {
            userDao.insert(it)
        }

    }
}