package com.munch.project.launcher.di

import com.munch.project.launcher.db.AppDao
import com.munch.project.launcher.db.DbHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Create by munch1182 on 2021/2/24 11:39.
 */
@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    @Provides
    fun provideDb(): DbHelper = DbHelper.getInstance()

    @Provides
    fun appDao(dbHelper: DbHelper): AppDao = dbHelper.getDb().appDao()
}