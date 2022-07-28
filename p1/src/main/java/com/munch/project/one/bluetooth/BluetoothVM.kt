package com.munch.project.one.bluetooth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munch.lib.extend.toLive
import kotlinx.coroutines.flow.MutableSharedFlow

internal class BluetoothVM : ViewModel() {

    private val intent = MutableSharedFlow<BleIntent>()
    private val _data = MutableLiveData<BleUIState>()
    val data = _data.toLive()
    private val list = mutableListOf<Dev>()
}