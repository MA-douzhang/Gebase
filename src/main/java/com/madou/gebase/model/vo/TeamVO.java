package com.madou.gebase.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 查询队伍信息的返回包装类
 */
@Data
public class TeamVO implements Serializable {

    /**
     * 队伍id
     */
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
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 状态 0-正常 1-私有  2-加密
     */
    private Integer teamState;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人用户信息
     */
    private UserVO createUser;
    /**
     * 队伍加入的人数
     */
    private Integer hasJoinNum;
    /**
     * 加入队伍的用户信息
     */
    private List<UserVO> userJoinList;

    /**
     * 队伍任务的信息
     */
    private List<TeamTaskVO> teamTaskList;

}
