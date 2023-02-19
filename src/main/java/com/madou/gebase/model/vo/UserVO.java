package com.madou.gebase.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

@Data
public class UserVO {
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
     * 用户自我介绍
     */
    private String userProfile;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 信箱
     */
    private String email;
    /**
     * 标签
     */
    private String tags;

    /**
     * 状态 0 正常
     */
    private Integer userState;

    /**
     * 0 - 普通用户 1 - 管理员

     */
    private Integer userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;



}
