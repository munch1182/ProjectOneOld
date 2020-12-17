package com.munch.project.testsimple.jetpack

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.jetpack.net.Api
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Create by munch1182 on 2020/12/17 15:29.
 */
@AndroidEntryPoint
class TestJetpackActivity : TestBaseTopActivity() {

    @Inject
    lateinit var api: Api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenResumed {
            api.getArticleList(0)
                .flowOn(Dispatchers.IO)
                .collect { }
        }
    }

}