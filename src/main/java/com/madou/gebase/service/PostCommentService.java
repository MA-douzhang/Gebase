package com.madou.gebase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.madou.gebase.model.PostComment;
import com.madou.gebase.model.vo.PostCommentVO;

import java.util.List;

/**
* @author MA_dou
* @description 针对表【post_comment(帖子)】的数据库操作Service
* @createDate 2023-02-25 17:17:07
*/
public interface PostCommentService extends IService<PostComment> {
    /**
     * 根据帖子id获取评论
     * @param postId
     * @return
     */
    List<PostCommentVO> getPostCommentVOList(Long postId);

    /**
     *  根据帖子id获取评论（查询缓存）
     * @param postId
     * @return
     */
    List<PostCommentVO> getPostCommentVOListCache(Long postId);
}
