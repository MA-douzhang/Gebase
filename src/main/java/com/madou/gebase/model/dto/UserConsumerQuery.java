package com.madou.gebase.model.dto;

import lombok.Data;

@Data
public class UserConsumerQuery {
    /**
     * 用户id
     */
    private long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;



}
