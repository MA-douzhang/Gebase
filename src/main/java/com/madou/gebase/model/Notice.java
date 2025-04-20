package com.madou.gebase.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 通知
 * @TableName notice
 */
@TableName(value ="notice")
@Data
public class Notice implements Serializable {
    /**
     * 消息id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送者id
     */
    private Long senderId;

    /**
     * 接收者id
     */
    private Long receiverId;

    /**
     * 信息体(最大200字)
     */
    private String content;

    /**
     * 信息类型，1为评论，2为回复评论，3为点赞，4系统通知，5队伍任务评论
     */
    private Integer contentType;

    /**
     * 信息体对象，帖子id等
     */
    private Long targetId;

    /**
     * 状态 0未读，1已读
     */
    private Integer noticeState;

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
