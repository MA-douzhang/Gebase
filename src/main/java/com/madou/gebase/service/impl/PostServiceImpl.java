package com.madou.gebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.madou.gebase.common.ErrorCode;
import com.madou.gebase.exception.BusinessException;
import com.madou.gebase.mapper.PostMapper;
import com.madou.gebase.model.Post;
import com.madou.gebase.model.PostComment;
import com.madou.gebase.model.User;
import com.madou.gebase.model.request.PostCommentAddRequest;
import com.madou.gebase.model.request.PostUpdateRequest;
import com.madou.gebase.model.vo.PostCommentVO;
import com.madou.gebase.model.vo.PostVO;
import com.madou.gebase.service.PostCommentService;
import com.madou.gebase.service.PostService;
import com.madou.gebase.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author MA_dou
 * @description 针对表【post(帖子)】的数据库操作Service实现
 * @createDate 2023-02-25 17:14:17
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {

    @Resource
    UserService userService;
    @Resource
    PostCommentService postCommentService;

    @Override
    public long addPost(Post post, User loginUser) {
        //请求参数是否为空
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //用户是否登录
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }
        final long userId = loginUser.getId();
        //内容内容大于10字小于200字
        String content = post.getContent();
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容为空");
        }
        if (content.length() < 10 || content.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容字数不符合要求");
        }
        //todo 判断敏感字符
        //信息插入帖子表
        post.setId(null);
        post.setUserId(userId);
        boolean result = this.save(post);
        Long postId = post.getId();
        if (!result || postId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "发布帖子失败");
        }
        return postId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePost(Long id, User loginUser) {
        Post post = this.getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }
        //是否为帖子创建人或者管理员
        Long userId = post.getUserId();
        if (!userService.isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        QueryWrapper<PostComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("postId", id);
        boolean remove = postCommentService.remove(queryWrapper);
        if (!remove) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除帖子失败");
        }
        return this.removeById(id);
    }

    @Override
    public boolean updatePost(PostUpdateRequest postUpdateRequest, User loginUser) {
        Post oldPost = this.getById(postUpdateRequest.getId());
        if (oldPost == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }
        Long userId = oldPost.getUserId();
        //是否为帖子创建人或者管理员
        if (!userService.isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        String content = postUpdateRequest.getContent();
        if (StringUtils.isBlank(content) || content.length() < 10 || content.length() > 600) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容字数不符合要求");
        }
        Post post = new Post();
        BeanUtils.copyProperties(postUpdateRequest, post);
        return this.updateById(post);
    }

    @Override
    public PostVO getPostInfoById(Long id) {
        Post post = this.getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "贴子不存在");
        }
        //帖子脱敏
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        //查帖子的创建人信息
        User createUser = userService.getById(postVO.getUserId());
        postVO.setUsername(createUser.getUsername());
        postVO.setAvatarUrl(createUser.getAvatarUrl());
        //查询帖子评论
        Long postVOId = postVO.getId();
        QueryWrapper<PostComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("postId", postVOId);
        List<PostComment> postCommentList = postCommentService.list(queryWrapper);
        List<PostCommentVO> postCommentVOList = new ArrayList<>();
        //帖子有评论就加载评论
        if (postCommentList != null && postCommentList.size() > 0) {
            //查询评论用户的信息
            List<Long> userIdList = postCommentList.stream().map(PostComment::getUserId).collect(Collectors.toList());
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.in("id", userIdList);
            // userId -> user 用户id对应用户信息
            Map<Long, List<User>> userListMap = userService.list(userQueryWrapper)
                    .stream().collect(Collectors.groupingBy(User::getId));
            //将查出来的用户信息与评论信息对接
            //将信息复制到返回类中
            postCommentList.forEach(postComment -> {
                PostCommentVO postCommentVO = new PostCommentVO();
                BeanUtils.copyProperties(postComment, postCommentVO);
                postCommentVOList.add(postCommentVO);
            });
            //将用户信息对接给评论
            postCommentVOList.forEach(postCommentVO -> {
                User user = userListMap.get(postCommentVO.getUserId()).get(0);
                postCommentVO.setUsername(user.getUsername());
                postCommentVO.setAvatarUrl(user.getAvatarUrl());
            });
        }
        postVO.setPostCommentList(postCommentVOList);
        return postVO;
    }


    @Override
    public boolean addComment(PostCommentAddRequest postCommentAddRequest, User loginUser) {
        if (postCommentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //帖子id存在
        Long postId = postCommentAddRequest.getPostId();
        if (postId == null || postId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }
        //内容字数小于200，内容不能为空
        String content = postCommentAddRequest.getContent();
        if (StringUtils.isBlank(content) || content.length() == 0 || content.length() >= 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容字数不符合要求");
        }
        //判断评论的pid,pid为null代表该条评论pid是帖子的创建者，反之是回复者的id

        PostComment postComment = new PostComment();
        BeanUtils.copyProperties(postCommentAddRequest, postComment);
        //判断评论的父id
        Long pid = postComment.getPid();
        //获取帖子的创建者id
        Post post = this.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }
        //默认为评论的父id为帖子的创建者
        if (pid == null) {
            postComment.setPid(post.getUserId());
        }
        final long userId = loginUser.getId();
        postComment.setUserId(userId);
        boolean result = postCommentService.save(postComment);
        return result;
    }

    @Override
    public boolean deleteComment(long id, User loginUser) {
        PostComment postComment = postCommentService.getById(id);
        if (postComment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }
        Long userId = postComment.getUserId();
        Long postId = postComment.getPostId();
        Post post = this.getById(postId);
        //是否为评论的创建人或者管理员，帖子的创建者
        if (!userService.isAdmin(loginUser) && userId != loginUser.getId() && post.getUserId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        return postCommentService.removeById(id);
    }
}




