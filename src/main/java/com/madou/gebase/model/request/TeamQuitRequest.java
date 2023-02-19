package com.madou.gebase.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 退出队伍请求体
 */
@Data
public class TeamQuitRequest implements Serializable {

    /**
     * 队伍id
     */
    private Long teamId;

}
