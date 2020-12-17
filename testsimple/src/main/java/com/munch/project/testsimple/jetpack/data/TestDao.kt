package com.munch.project.testsimple.jetpack.data

import android.os.Parcelable
import androidx.paging.DataSource
import androidx.room.*
import com.munch.project.testsimple.App
import kotlinx.parcelize.Parcelize

/**
 * Create by munch1182 on 2020/12/17 15:54.
 */
@Parcelize
@Entity
data class User(
    @PrimaryKey var id: Long = 0L,
    @ColumnInfo(name = "name") var name: String = ""
) : Parcelable {

    companion object {
        private var lastId = 0L

        fun newId(): Long {
            lastId++
            return lastId
        }

        fun newInstance() = User(newId(), "123")

        fun newArrayInstance() = Array(15 * 3) { newInstance() }
    }
}

@Dao
interface UserDao {

    @Query("SELECT * FROM user LIMIT :page,15")
    fun queryData(page: Int): DataSource.Factory<Int, User>

    @Query("SELECT * FROM user")
    fun queryAllData(): DataSource.Factory<Int, User>

    @Query("SELECT * FROM user WHERE id LIKE :id OR name LIKE :name")
    fun queryUser(id: Long?, name: String?): User

    @Insert
    fun insert(user: User)

    @Delete
    fun del(user: User)
}

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

object DbHelper {

    private val roomDb by lazy {
        Room.databaseBuilder(
            App.getInstance(),
            AppDatabase::class.java,
            "db_user"
        ).build()
    }

    fun getRoom() = roomDb

}

/*
class UserPagingSource() : PagingSource<Int, User>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        return LoadResult.Page(User.newArrayInstance(),)
    }
}*/