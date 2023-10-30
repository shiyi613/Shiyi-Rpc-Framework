package com.shiyi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;


@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {

    SUCCESS(200, "The remote call is successful"),
    FAIL(500, "The remote call is fail"),
    CLIENT_TIMEOUT(501,"The remote call timeout about client"),
    SERVER_TIMEOUT(502,"The remote call timeout about server"),
    FUTURE_CANCELLED(503,"The result future about the remote call is cancelled"),
    THREAD_INTERRUPTED(504,"The thread about the remote call is interrupted");

    private final int code;

    private final String message;

}
