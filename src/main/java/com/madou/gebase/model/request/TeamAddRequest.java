package com.madou.gebase.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录请求体
 */
@Data
public class TeamAddRequest implements Serializable {

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
     * 最大人数
     */
    private Integer maxNum;

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
