package com.madou.gebase.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求体
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -5602452186149132432L;
    String userAccount;
    String userPassword;
}
