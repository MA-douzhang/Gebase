package com.madou.gebase.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求体
 */
@Data
public class ObjectIdRequest implements Serializable {
    /**
     * 删除对象 id
     */
    private Long id;
}
