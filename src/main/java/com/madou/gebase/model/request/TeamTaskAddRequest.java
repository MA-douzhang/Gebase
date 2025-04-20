package com.madou.gebase.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录请求体
 */
@Data
public class TeamTaskAddRequest implements Serializable {

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String description;

}
