package com.shiyi.exception;


import com.shiyi.enums.RpcErrorMessageEnum;


public class RpcException extends RuntimeException {

    public RpcException(String msg){
        super(msg);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }
}
