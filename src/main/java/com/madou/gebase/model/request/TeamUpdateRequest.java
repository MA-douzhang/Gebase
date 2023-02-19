package com.madou.gebase.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍更新封装类
 */
@Data
public class TeamUpdateRequest implements Serializable {

    private Long id;
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 队伍密码
     */
    private String teamPassword;

    /**
     * 状态 0-正常 1-私有  2-加密
     */
    private Integer teamState;

    /**
     * 过期时间
     */
    private Date expireTime;


}
