package com.madou.gebase.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询通知信息返回封装
 * @TableName noticeVO
 */

@Data
public class NoticeVO implements Serializable {

    /**
     * 消息id
     */
    private Long id;

    /**
     * 发送者的用户名
     */
    private String senderName;

    /**
     * 接收者用户名
     */
    private String receiverName;

    /**
     * 信息体(最大200字)
     */
    private String content;

    /**
     * 信息类型，1为评论，2为回复评论，3为点赞，4系统通知
     */
    private Integer contentType;

    /**
     * 信息体对象，帖子内容等
     */
    private String targetContent;

    /**
     * 状态 0未读，1已读
     */
    private Integer noticeState;

    private static final long serialVersionUID = 1L;
}
