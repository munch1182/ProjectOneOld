package com.munch.project.one.result

/**
 * Create by munch1182 on 2022/5/21 16:59.
 */

internal sealed class ResultIntent {

    object Refresh : ResultIntent()
    object Request : ResultIntent()

    class Selected(val pi: PI) : ResultIntent()
}

internal sealed class ResultUIState {

    class Data(val data: List<PI>) : ResultUIState()
}