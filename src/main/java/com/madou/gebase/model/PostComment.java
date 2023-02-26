package com.madou.gebase.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子
 * @TableName post_comment
 */
@TableName(value ="post_comment")
@Data
public class PostComment implements Serializable {
    /**
     * 评论id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评论用户id
     */
    private Long userId;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
