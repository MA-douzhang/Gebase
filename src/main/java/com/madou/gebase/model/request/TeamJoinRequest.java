package com.madou.gebase.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录请求体
 */
@Data
public class TeamJoinRequest implements Serializable {

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 队伍密码
     */
    private String teamPassword;

}
