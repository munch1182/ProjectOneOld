package com.munch.module.template.dto;

import androidx.annotation.Nullable;
import com.munch.module.template.net.NetConfig;

/**
 * Created by Munch on 2019/7/26 16:14
 */
public class BDto<T> {

    private String code;
    private String msg;
    private T data;

    public String getCode() {
        return code == null ? "" : code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg == null ? "" : msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Nullable
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return NetConfig.Code.SUCCESS.equals(code);
    }
}
