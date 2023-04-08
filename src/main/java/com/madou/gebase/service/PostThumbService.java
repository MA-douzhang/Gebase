package com.madou.gebase.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.madou.gebase.model.PostThumb;
import com.madou.gebase.model.User;

import java.util.List;

/**
* @author MA_dou
* @description 针对表【post_thumb(帖子点赞)】的数据库操作Service
* @createDate 2023-04-01 12:26:07
*/
public interface PostThumbService extends IService<PostThumb> {

    /**
     * 给帖子点赞
     * @param postId
     * @param loginUser
     * @return
     */
    int doPostThumb(long postId, User loginUser);

    /**
     * 帖子点赞（内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doPostThumbInner(long userId, long postId);

    /**
     * 获取该用户点赞的帖子列表
     * @param loginUser
     * @return
     */
    List<Long> getUserPostThumb(User loginUser);

    /**
     * 判断当前登录用户是否点赞该帖子(查询缓存)
     *
     * @param id
     * @param userId
     * @return true是存在该帖子的点赞
     */
    boolean isPostThumb(Long id, long userId);
}
