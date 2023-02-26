package com.madou.gebase.controller;

import com.madou.gebase.common.BaseResponse;
import com.madou.gebase.common.ErrorCode;
import com.madou.gebase.common.ResultUtils;
import com.madou.gebase.exception.BusinessException;
import com.madou.gebase.model.Post;
import com.madou.gebase.model.User;
import com.madou.gebase.model.request.ObjectIdRequest;
import com.madou.gebase.model.request.PostAddRequest;
import com.madou.gebase.model.request.PostCommentAddRequest;
import com.madou.gebase.model.request.PostUpdateRequest;
import com.madou.gebase.model.vo.PostVO;
import com.madou.gebase.service.PostCommentService;
import com.madou.gebase.service.PostService;
import com.madou.gebase.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子接口
 *
 * @author MA_dou
 */
@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {
    @Resource
    PostService postService;

    @Resource
    UserService userService;

    @Resource
    PostCommentService postCommentService;

    /**
     * 添加帖子
     * @param postAddRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addPost(@RequestBody PostAddRequest postAddRequest, HttpServletRequest httpServletRequest) {
        if (postAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        Post post = new Post();
        BeanUtils.copyProperties(postAddRequest,post);
        long postId = postService.addPost(post,loginUser);
        return ResultUtils.success(postId);
    }

    /**
     * 删除帖子
     *
     * @param objectIdRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePost(@RequestBody ObjectIdRequest objectIdRequest, HttpServletRequest httpServletRequest) {
        if (objectIdRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = objectIdRequest.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = postService.deletePost(id,loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新帖子
     * @param postUpdateRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePost(@RequestBody PostUpdateRequest postUpdateRequest, HttpServletRequest httpServletRequest) {
        if (postUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = postService.updatePost(postUpdateRequest,loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据更新失败");
        }
        return ResultUtils.success(true);
    }
    /**
     * 查询帖子
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<PostVO> getTeamById(@RequestParam long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        PostVO postVO = postService.getPostInfoById(id);
        if (postVO == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询帖子失败");
        }
        return ResultUtils.success(postVO);
    }

    /**
     * 添加帖子评论
     * @param postCommentAddRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/addComment")
    public BaseResponse<Boolean> addPostComment(@RequestBody PostCommentAddRequest postCommentAddRequest, HttpServletRequest httpServletRequest) {
        if (postCommentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = postService.addComment(postCommentAddRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除帖子评论
     * @param objectIdRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/deleteComment")
    public BaseResponse<Boolean> deleteComment(@RequestBody ObjectIdRequest objectIdRequest, HttpServletRequest httpServletRequest) {
        if (objectIdRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = objectIdRequest.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = postService.deleteComment(id,loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }
}
