package com.shiyi.remoting.dto;

import com.shiyi.enums.RpcResponseCodeEnum;
import io.protostuff.Rpc;
import lombok.*;

import java.io.Serializable;

/**
 * @Author:shiyi
 * @create: 2023-05-17  18:22
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 9099806406420982535L;

    /**
     * request Id
     */
    private String requestId;

    /**
     * response code
     */
    private Integer code;

    /**
     * response message
     */
    private String message;

    /**
     * response body
     */
    private T data;

    public RpcResponse(String requestId){
        this.requestId = requestId;
    }

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RpcResponse<T> fail(String requestId, RpcResponseCodeEnum rpcResponseCodeEnum) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setRequestId(requestId);
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }


}
