package com.madou.gebase.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

@Data
public class Message {

    // 发送者name
    private String userAccount;
    // 接收者name
    private String consumer;
    // 发送的文本
    private String content;
    // 发送的头像
    private String userAvatar;
    // 发送时间
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    public Date date;

}
