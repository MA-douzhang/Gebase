package com.madou.gebase.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求体
 */
@Data
public class PostAddRequest implements Serializable {

    /**
     * 内容
     */
    private String content;

}
