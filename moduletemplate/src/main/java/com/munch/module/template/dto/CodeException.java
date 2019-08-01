package com.munch.module.template.dto;

import androidx.annotation.NonNull;
import com.munch.lib.libnative.excetion.BException;

/**
 * Created by Munch on 2019/7/26 16:15
 */
public class CodeException extends BException {

    private String code;

    public CodeException(String code, String message) {
        this(true, code, message);
    }

    public CodeException(boolean canHandle, String code, String message) {
        super(canHandle, message);
        this.code = code;
    }

    public CodeException(boolean canHandle, @NonNull BDto dto) {
        this(canHandle, dto.getCode(), dto.getMsg());
    }

    public BDto getBDto() {
        BDto bDto = new BDto();
        bDto.setCode(code);
        bDto.setMsg(getMessage());
        return bDto;
    }
}
