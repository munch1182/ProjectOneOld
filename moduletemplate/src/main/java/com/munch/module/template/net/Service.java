package com.munch.module.template.net;

import com.munch.module.template.dto.BDto;
import io.reactivex.Observable;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Munch on 2019/7/26 14:10
 */
public interface Service {

    @POST()
    Observable<BDto<Object>> login(@Query("user") String userName, @Query("pwd") String pwd);
}
