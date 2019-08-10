package com.munch.module.test.jatpack

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.lifecycle.*
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.munch.lib.libnative.helper.AppHelper
import com.munch.lib.libnative.root.RootActivity
import com.munch.lib.log.LogLog
import com.munch.module.test.R
import kotlinx.android.synthetic.main.activity_jet.*
import retrofit2.Response
import kotlin.concurrent.thread
import kotlin.random.Random

/**
 * Created by Munch on 2019/8/9 10:43
 */
class JetActivity : RootActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jet)
        val provider = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.application))
        val viewModel = provider.get(UserModel::class.java)
        val loginCountModel = provider.get(LoginCountModel::class.java)


        viewModel.userLiveData.value = User().apply {
            name = "233"
        }
        viewModel.getUser().observe(this, Observer {
            val name = it.name ?: return@Observer
            tv_jet_name.text = name
        })

        /*loginCountModel.get().observe(this, Observer {
            tv_jet_name.text = tv_jet_name.text.toString().plus(it.count)
        })*/

        btn_jet_change.setOnClickListener {
            viewModel.newName()
            loginCountModel.add()
            thread {
                LogLog.log(Db.getInstance().getLoginCountDao().query())
            }
        }


    }

    class UserRepository {


    }

    class Status constructor(val code: Int) {

        constructor() : this(-1)
    }

    class Resource<T> constructor(val status: Status, val data: T, val message: String? = null) {

        companion object {

            fun <T> success(@NonNull data: T): Resource<T> {
                return Resource(Status(0), data)
            }

            fun <T> error(@NonNull message: String, @Nullable data: T?): Resource<T?> {
                return Resource(Status(-1), data, message)
            }

            fun <T> loading(@Nullable data: T? = null): Resource<T?> {
                return Resource(Status(-2), data)
            }
        }
    }


    abstract class NetworkBoundResource<ResultType, RequestType> {

        val result = MediatorLiveData<Resource<ResultType?>>()

        init {
            result.value = Resource.loading()
            val dbSource = loadFromDb()
            result.addSource(dbSource) {
                result.removeSource(dbSource)
                if (shouldFetch(it)) {
                    fetchFromNetWork(dbSource)
                } else {
                    result.addSource(dbSource) { a ->
                        result.value = Resource.success(a)
                    }
                }
            }
        }

        fun fetchFromNetWork(dbSource: MutableLiveData<ResultType>) {
            val apiResponse = createCall()
            result.addSource(dbSource) {
                result.value = Resource.loading(it)
            }
            result.addSource(apiResponse) {
                result.removeSource(apiResponse)
                result.removeSource(dbSource)
                if (it.isSuccessful) {
                    saveResultAndReInit(it)
                } else {
                    onFetchFailed()
                    result.addSource(dbSource) { a ->
                        Resource.error("", a)
                    }
                }
            }

        }

        private fun onFetchFailed() {

        }

        private fun saveResultAndReInit(response: Response<ResultType>?) {
            thread {
                saveCallResult(response?.body())
                Handler(Looper.getMainLooper()).post {
                    result.addSource(loadFromDb()) {
                        result.value = Resource.success(it)
                    }
                }
            }
        }

        //save 2 db
        fun saveCallResult(body: ResultType?) {}

        fun createCall(): LiveData<Response<ResultType>> {
            return MutableLiveData<Response<ResultType>>()
        }

        fun shouldFetch(it: ResultType): Boolean {
            return true
        }

        fun loadFromDb(): MutableLiveData<ResultType> = MutableLiveData()
    }


    class UserModel : ViewModel() {

        var userLiveData = MutableLiveData<User>()

        fun getUser() = userLiveData

        fun newName() {
            val value = userLiveData.value ?: User().apply { name = "233" }
            value.name = Random.nextInt().toString()
            userLiveData.value = value
        }

    }

    class LoginCountModel : ViewModel() {

        var loginCountLiveData = MutableLiveData<LoginCount>()

        fun get() = loginCountLiveData

        fun add() {
            val value = get().value ?: LoginCount()
            value.count++
            get().value = value
            thread {
                Db.getInstance().getLoginCountDao().login(get().value!!)
            }
        }
    }

    object Db {

        fun getInstance(): AppDb {
            return Room.databaseBuilder(AppHelper.getContext(), AppDb::class.java, "user.db").build()
        }
    }

    @Database(entities = [LoginCount::class], version = 1)
    abstract class AppDb : RoomDatabase() {
        abstract fun getLoginCountDao(): UserDao
    }

    @Dao
    interface UserDao {

        @Insert(onConflict = REPLACE)
        fun login(user: LoginCount)

        @Query("select * from login_count where count = :count")
        fun query(count: Long): LiveData<LoginCount>

        @Query("select * from login_count")
        fun query(): List<LoginCount>

    }

    class User {
        var name: String? = null
    }

    @Entity(tableName = "login_count")
    class LoginCount {
        @PrimaryKey
        var count = 0L
        var time = System.currentTimeMillis()

        override fun toString(): String {
            return "LoginCount(count=$count, time=$time)"
        }


    }

}