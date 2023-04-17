package com.madou.gebase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.madou.gebase.model.Post;
import com.madou.gebase.model.User;
import com.madou.gebase.model.request.PostCommentAddRequest;
import com.madou.gebase.model.request.PostUpdateRequest;
import com.madou.gebase.model.vo.PostVO;

/**
* @author MA_dou
* @description 针对表【post(帖子)】的数据库操作Service
* @createDate 2023-02-25 17:14:17
*/
public interface PostService extends IService<Post> {
    /**
     * 发布帖子
     * @param postAddRequest
     * @param loginUser
     * @return
     */
    long addPost(Post postAddRequest, User loginUser);

    /**
     * 删除帖子
     * @param id
     * @param loginUser
     * @return
     */
    boolean deletePost(Long id, User loginUser);

    /**
     * 更新帖子
     * @param postUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updatePost(PostUpdateRequest postUpdateRequest, User loginUser);

    /**
     * 查询帖子
     * @param id
     * @return
     */
    PostVO getPostInfoById(Long id);

    /**
     * 查询帖子详细信息
     * @param id
     * @return
     */
    PostVO getPostInfoById(Long id,boolean isInfo);
    /**
     * 添加帖子评论(同时更新缓存)
     * @param postCommentAddRequest
     * @param loginUser
     * @return
     */
    boolean addComment(PostCommentAddRequest postCommentAddRequest, User loginUser);

    /**
     * 删除帖子评论
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteComment(long id, User loginUser);
}
