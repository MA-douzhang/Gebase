package com.madou.gebase.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 队伍更新封装类
 */
@Data
public class PostUpdateRequest implements Serializable {

    /**
     * 帖子id
     */
    private Long id;

    /**
     * 内容
     */
    private String content;


}
