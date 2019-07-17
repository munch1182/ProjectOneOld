package com.munch.module.rxjava

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.NetworkOnMainThreadException
import android.widget.Toast
import com.munch.lib.log.LogLog
import com.munch.lib.test.TestBaseActivity
import com.munch.lib.test.app.TestApp
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.reactivestreams.Subscription
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
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
            service.today()
                .compose {
                    it.subscribeOn(Schedulers.io())
                        .map { res -> res.toString() }
                        .lift(OperatorHttpResult<Boolean, String>())
                        .observeOn(AndroidSchedulers.mainThread())
                }
                .subscribe { res -> LogLog.log(res) }

        }
    }

    class OperatorHttpResult<D, U> : ObservableOperator<D, U> {

        override fun apply(observer: Observer<in D>): Observer<in U> {
            return HttpResultSubscriber<D, U>(observer)
        }
    }

    class HttpResultSubscriber<D, U>(private val observer: Observer<in D>) : Observer<U> {
        override fun onComplete() {
            try {
                observer.onComplete()
            } finally {

            }
        }

        override fun onSubscribe(d: Disposable) {
            observer.onSubscribe(d)
        }

        override fun onNext(t: U) {
            val contains = (t as String).contains("munch")
            if (!contains) {
                throw RuntimeException("没找到")
            } else {
                observer.onNext(contains as D)
            }
        }

        override fun onError(e: Throwable) {
            LogLog.log(e)
            try {
                when (e) {
                    is RuntimeException -> LogLog.log(e.message)
                    is OnErrorNotImplementedException -> {

                    }
                    else -> observer.onError(e)
                }
            } catch (e: OnErrorNotImplementedException) {

            } finally {

            }
        }
    }


    object NetManager {

        private val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("http://gank.io/api/")
            .build()

        fun getService() = retrofit.create(Gank::class.java)
    }

    interface Gank {

        @GET("today")
        fun today(): Observable<Any>
    }
}
