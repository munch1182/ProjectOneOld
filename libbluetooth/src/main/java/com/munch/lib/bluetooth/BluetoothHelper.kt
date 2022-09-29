package com.munch.lib.bluetooth

import com.munch.lib.android.extend.ScopeContext
import com.munch.lib.android.log.Logger
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2022/9/29 14:25.
 */
object BluetoothHelper : ScopeContext,
    IBluetoothManager by BluetoothEnv,
    IBluetoothState by BluetoothEnv {

    internal val log = Logger("bluetooth")

    private val appJob = SupervisorJob()
    private val appJobName = CoroutineName("bluetooth")


    override val coroutineContext: CoroutineContext = appJob + appJobName + Dispatchers.Default
}