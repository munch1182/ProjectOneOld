package com.munch.module.rxjava

import android.annotation.SuppressLint
import android.os.Bundle
import com.munch.lib.log.LogLog
import com.munch.lib.test.TestBaseActivity
import io.reactivex.Observable
import io.reactivex.ObservableOperator
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import java.lang.RuntimeException

/**
 * Created by Munch on 2019/7/16 17:23
 */
class MainActivity : TestBaseActivity() {

    private val service = NetManager.getService()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        request.setOnClickListener {
            service.getRegAgreement()
                .subscribeOn(Schedulers.io())
                .lift(ObservableOperator<Any, BData<Any>> { observer -> ResDtoObserver(observer) })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { res -> LogLog.log(res) }

        }
    }

    class BData<T> {
        var code = "1"
        var msg = "成功"
        var data: T? = null
    }

    class CodeE : RuntimeException() {}

    class ResDtoObserver<T, D>(val downObserver: Observer<in T>) : Observer<D> {

        override fun onComplete() {
            downObserver.onComplete()
        }

        override fun onSubscribe(d: Disposable) {
            downObserver.onSubscribe(d)
        }

        override fun onNext(t: D) {
            if (t is BData<*>) {
                if (t.code != "0") {
                    onError(CodeE())
                    return
                }
                val t1 = t.data as? T?
                if (t1 == null) {
                    downObserver.onComplete()
                } else {
                    downObserver.onNext(t1)
                }
            } else {
                throw CodeE()
            }
        }

        override fun onError(e: Throwable) {
            try {
                //拦截上游的错误，让下游的OnErrorNotImplementedException不触发，
                //但同时这两个上游的错误也不会触发下游的onError，因此要区别对待
                if (e is CodeE) {
                    LogLog.log(e)
                } else if (e is OnErrorNotImplementedException) {

                } else {
                    downObserver.onError(e)
                }
            } catch (e: OnErrorNotImplementedException) {
                e.printStackTrace()
            }
        }
    }

    object NetManager {

        private val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("http://120.79.241.184:8081/WalkieTalkie/")
            .build()

        fun getService() = retrofit.create(Gank::class.java)
    }

    interface Gank {

        @POST("cus/language/getLanguageList")
        fun getRegAgreement(): Observable<BData<Any>>
    }
}
