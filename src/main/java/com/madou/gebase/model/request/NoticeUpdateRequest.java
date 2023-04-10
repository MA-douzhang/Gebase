package com.madou.gebase.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 通知更新请求体
 */
@Data
public class NoticeUpdateRequest implements Serializable {

    /**
     * 帖子id
     */
    private Long id;

    /**
     * 通知状态 0为未读 1已读
     */
    private Integer noticeState;

}
