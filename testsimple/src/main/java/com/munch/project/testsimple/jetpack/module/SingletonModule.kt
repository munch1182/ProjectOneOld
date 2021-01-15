package com.munch.project.testsimple.jetpack.module

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.munch.lib.log
import com.munch.project.testsimple.jetpack.db.ArticleDao
import com.munch.project.testsimple.jetpack.db.Db
import com.munch.project.testsimple.jetpack.db.PageDao
import com.munch.project.testsimple.jetpack.net.Api
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Create by munch1182 on 2020/12/17 21:46.
 */
@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    private const val BASE_URL = "https://www.wanandroid.com"
    private const val NAME_DB = "db_article"

    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            /*.addCallAdapterFactory(FlowCallAdapterFactory.create())*/
            .client(client)
            .build()
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                addInterceptor(HttpLoggingInterceptor { log(it) }.apply {
                    setLevel(HttpLoggingInterceptor.Level.BASIC)
                })
            }
            .build()
    }

    @Provides
    fun provideApi(retrofit: Retrofit): Api = retrofit.create(Api::class.java)

    @Provides
    fun provideDb(app: Application): Db {
        return Room.databaseBuilder(app, Db::class.java, NAME_DB)
            .addMigrations(Migration1to2(), Migration2to1())
            .build()
    }

    @Provides
    fun provideArticleDao(db: Db): ArticleDao = db.articleDao()

    @Provides
    fun providePageDao(db: Db): PageDao = db.pageDao()

    /**
     * 升级时不允许null的值需要提供给以前的版本默认值
     * 不带not null的即视为可为null
     *
     * 注意保存schemas中的sql的json文件
     */
    class Migration1to2 : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE ${Db.TB_NAME_PAGE} ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'")
        }
    }

    /**
     * 通过复制新表并重命名的方式删除列
     *
     *
     * 如果真要写，注意复制schemas的语句，以及批量替换
     */
    class Migration2to1 : Migration(2, 1) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("BEGIN TRANSACTION;")
            database.execSQL("CREATE TABLE IF NOT EXISTS `tb_article_backup` (`apkLink` TEXT NOT NULL, `audit` INTEGER NOT NULL, `author` TEXT NOT NULL, `canEdit` INTEGER NOT NULL, `chapterId` INTEGER NOT NULL, `chapterName` TEXT NOT NULL, `collect` INTEGER NOT NULL, `courseId` INTEGER NOT NULL, `desc` TEXT NOT NULL, `descMd` TEXT NOT NULL, `envelopePic` TEXT NOT NULL, `fresh` INTEGER NOT NULL, `id` INTEGER NOT NULL, `link` TEXT NOT NULL, `niceDate` TEXT NOT NULL, `niceShareDate` TEXT NOT NULL, `origin` TEXT NOT NULL, `prefix` TEXT NOT NULL, `projectLink` TEXT NOT NULL, `publishTime` INTEGER NOT NULL, `realSuperChapterId` INTEGER NOT NULL, `selfVisible` INTEGER NOT NULL, `shareDate` INTEGER NOT NULL, `shareUser` TEXT NOT NULL, `superChapterId` INTEGER NOT NULL, `superChapterName` TEXT NOT NULL, `title` TEXT NOT NULL, `type` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `visible` INTEGER NOT NULL, `zan` INTEGER NOT NULL, PRIMARY KEY(`id`));")
            database.execSQL("INSERT INTO tb_article_backup SELECT `apkLink` , `audit` , `author` , `canEdit` , `chapterId` , `chapterName` , `collect` , `courseId` , `desc` , `descMd` , `envelopePic` , `fresh` , `id` , `link` , `niceDate` , `niceShareDate` , `origin` , `prefix` , `projectLink` , `publishTime` , `realSuperChapterId` , `selfVisible` , `shareDate` , `shareUser` , `superChapterId` , `superChapterName` , `title` , `type` , `userId` , `visible` , `zan` FROM ${Db.TB_NAME_PAGE};")
            database.execSQL("DROP TABLE ${Db.TB_NAME_PAGE};")
            database.execSQL("CREATE TABLE IF NOT EXISTS `tb_article` (`apkLink` TEXT NOT NULL, `audit` INTEGER NOT NULL, `author` TEXT NOT NULL, `canEdit` INTEGER NOT NULL, `chapterId` INTEGER NOT NULL, `chapterName` TEXT NOT NULL, `collect` INTEGER NOT NULL, `courseId` INTEGER NOT NULL, `desc` TEXT NOT NULL, `descMd` TEXT NOT NULL, `envelopePic` TEXT NOT NULL, `fresh` INTEGER NOT NULL, `id` INTEGER NOT NULL, `link` TEXT NOT NULL, `niceDate` TEXT NOT NULL, `niceShareDate` TEXT NOT NULL, `origin` TEXT NOT NULL, `prefix` TEXT NOT NULL, `projectLink` TEXT NOT NULL, `publishTime` INTEGER NOT NULL, `realSuperChapterId` INTEGER NOT NULL, `selfVisible` INTEGER NOT NULL, `shareDate` INTEGER NOT NULL, `shareUser` TEXT NOT NULL, `superChapterId` INTEGER NOT NULL, `superChapterName` TEXT NOT NULL, `title` TEXT NOT NULL, `type` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `visible` INTEGER NOT NULL, `zan` INTEGER NOT NULL, PRIMARY KEY(`id`));")
            database.execSQL("INSERT INTO ${Db.TB_NAME_PAGE} SELECT `apkLink` , `audit` , `author` , `canEdit` , `chapterId` , `chapterName` , `collect` , `courseId` , `desc` , `descMd` , `envelopePic` , `fresh` , `id` , `link` , `niceDate` , `niceShareDate` , `origin` , `prefix` , `projectLink` , `publishTime` , `realSuperChapterId` , `selfVisible` , `shareDate` , `shareUser` , `superChapterId` , `superChapterName` , `title` , `type` , `userId` , `visible` , `zan` FROM tb_article_backup;")
            database.execSQL("DROP TABLE tb_article_backup")
            database.execSQL("COMMIT")
        }
    }
}