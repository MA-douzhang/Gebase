package com.madou.gebase.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍任务评论
 * @TableName team_task_comment
 */
@Data
public class TeamTaskCommentVO implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评论用户id
     */
    private Long userId;
    /**
     * 用户昵称
     */
    private String username;

    /**
     * 头像
     */
    private String avatarUrl;
    /**
     * 评论任务id
     */
    private Long teamTaskId;

    /**
     * 评论内容(最大200字)
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;


}