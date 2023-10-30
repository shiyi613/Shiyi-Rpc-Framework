package com.shiyi.config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;

/**
 * rpc configuration class
 *
 * @Author:shiyi
 * @create: 2023-05-23  17:50
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcProperties {

    /**
     * Compress type
     */
    private String compressName;

    /**
     * LoadBalance type
     */
    private String loadBalance;

    /**
     * Register type
     */
    private String register;

    /**
     * Register center address(IP:PORT)
     */
    private String address;

    /**
     * Network transmission type
     */
    private String transporter;

    /**
     * Network bind port
     */
    private String serverPort;

    /**
     * Serialize type
     */
    private String serializer;


    public int getRegisterPort(){
        String[] arr = address.split(":");
        return Integer.parseInt(arr[1]);
    }



}
