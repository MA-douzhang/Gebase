package com.madou.gebase.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 帖子
 * @TableName post
 */
@Data
public class PostVO implements Serializable {
    /**
     * 帖子id
     */
    private Long id;

    /**
     * 内容
     */
    private String content;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 状态 0 正常
     */
    private Integer postState;

    /**
     * 帖子的评论
     */
    private List<PostCommentVO> postCommentList;

}
