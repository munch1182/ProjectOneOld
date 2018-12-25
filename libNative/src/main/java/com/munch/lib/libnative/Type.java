package com.munch.lib.libnative;

import android.support.annotation.IntDef;


/**
 * Created by Munch on 2018/12/25 23:54.
 */
@IntDef({Type.TYPE_DATA_FAIL, Type.TYPE_DATA_SUCCESS, Type.TYPE_UN_GET_ERROR, Type.TYPE_DATA_EMPTY})
public @interface Type {

    int TYPE_DATA_EMPTY = 2403;
    int TYPE_DATA_SUCCESS = 2355;
    int TYPE_DATA_FAIL = 2356;
    int TYPE_UN_GET_ERROR = 2357;
}
