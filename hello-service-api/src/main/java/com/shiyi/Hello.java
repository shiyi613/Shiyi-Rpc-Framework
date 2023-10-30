package com.shiyi;

import lombok.*;
import java.io.Serializable;

/**
 * @Author:shiyi
 * @create: 2023-05-19  0:01
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Hello implements Serializable {
    private String message;
    private String description;
}
