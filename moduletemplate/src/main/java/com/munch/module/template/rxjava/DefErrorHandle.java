package com.munch.module.template.rxjava;

import com.munch.module.template.dto.BDto;

/**
 * 处理所有rxjava的错误，{@link RxTransHelper}，未拦截的错误如果subscribe未调用onError，则会出现{@link io.reactivex.exceptions.OnErrorNotImplementedException}
 * Created by Munch on 2019/7/26 16:27
 */
public class DefErrorHandle {

    /**
     * @return true，拦截错误，不会传递到subscribe，传递错误到最后的subscribe
     */
    public static boolean handle(BDto dto) {
        return false;
    }

    /**
     * @return true，拦截错误，不会传递到subscribe，传递错误到最后的subscribe
     */
    public static boolean handle(Throwable e) {
        return true;
    }
}
