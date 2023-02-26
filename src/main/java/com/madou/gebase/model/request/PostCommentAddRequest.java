package com.madou.gebase.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求体
 */
@Data
public class PostCommentAddRequest implements Serializable {

    /**
     * 帖子id
     */
    private Long postId;

    /**
     * 内容
     */
    private String content;

    /**
     * 父id
     */
    private Long pid;

}
