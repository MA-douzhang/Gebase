package com.madou.gebase.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 帖子
 * @TableName post_comment
 */
@Data
public class PostCommentVO implements Serializable {
    /**
     * 评论id
     */
    private Long id;

    /**
     * 评论用户id
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 头像
     */
    private String avatarUrl;
    /**
     * 评论帖子id
     */
    private Long postId;

    /**
     * 评论内容(最大200字)
     */
    private String content;

    /**
     * 父id
     */
    private Long pid;

    /**
     * 状态 0 正常
     */
    private Integer commentState;



}
