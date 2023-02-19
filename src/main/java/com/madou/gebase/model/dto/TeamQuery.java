package com.madou.gebase.model.dto;

import com.madou.gebase.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 队伍查询封装类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {
    /**
     * 队伍id
     */
    private Long id;
    /**
     * 用户加入队伍Id
     */
    private List<Long> listUserId;

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 检索关键字（队伍名称和队伍描述）
     */
    private String searchText;

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



}
