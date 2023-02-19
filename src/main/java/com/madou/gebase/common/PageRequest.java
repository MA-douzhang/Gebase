package com.madou.gebase.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 */
@Data
public class PageRequest implements Serializable {


    /**
     * 页面大小
     */
    protected int pageSize;
    /**
     * 页面页数
     */
    protected int pageNum;
}
