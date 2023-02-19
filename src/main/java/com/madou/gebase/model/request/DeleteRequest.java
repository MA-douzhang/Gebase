package com.madou.gebase.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求体
 */
@Data
public class DeleteRequest implements Serializable {
    /**
     * 队伍id
     */
    private Long id;
}
