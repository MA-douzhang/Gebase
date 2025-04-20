package com.madou.gebase.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 队伍任务请求体
 */
@Data
public class TeamTaskCommentAddRequest implements Serializable {

    /**
     * 队伍任务id
     */
    private Long teamTaskId;

    /**
     * 内容
     */
    private String content;

}
