package com.madou.gebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.madou.gebase.common.ErrorCode;
import com.madou.gebase.contant.RedisConstant;
import com.madou.gebase.exception.BusinessException;
import com.madou.gebase.mapper.PostCommentMapper;
import com.madou.gebase.model.PostComment;
import com.madou.gebase.model.User;
import com.madou.gebase.model.vo.PostCommentVO;
import com.madou.gebase.service.PostCommentService;
import com.madou.gebase.service.UserService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author MA_dou
* @description 针对表【post_comment(帖子)】的数据库操作Service实现
* @createDate 2023-02-25 17:17:07
*/
@Service
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment>
    implements PostCommentService{

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public List<PostCommentVO> getPostCommentVOList(Long postId) {
        if (postId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //反复抢锁 保证数据一致性
        List<PostCommentVO> postCommentVOList = new ArrayList<>();
        //获取锁
        RLock lock = redissonClient.getLock(RedisConstant.REDIS_POST_COMMENT_KEY);
        try {
            while (true){
                //反复抢锁，保证数据一致性
                if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                    //当前获得锁的线程的id是
                    System.out.println("getLock"+Thread.currentThread().getId());
                    //查询信息
                    QueryWrapper<PostComment> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("postId", postId);
                    List<PostComment> postCommentList = this.list(queryWrapper);
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
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    //写缓存
                    try {
                        valueOperations.set(RedisConstant.REDIS_POST_COMMENT_KEY+postId,postCommentVOList);
                    } catch (Exception e) {
                        log.error("redis set key error",e);
                    }
                    return postCommentVOList;
                }
            }
        } catch (InterruptedException e) {
            log.error("getPostCommentVOList error", e);
        }finally {
            //只能自己释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
        return postCommentVOList;

    }

    @Override
    public List<PostCommentVO> getPostCommentVOListCache(Long postId) {
        //查询缓存
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        List<PostCommentVO> postCommentVOList= (List<PostCommentVO>) valueOperations.get(RedisConstant.REDIS_POST_COMMENT_KEY+postId);
        //缓存为空，查询数据库并写入缓存,不为空返回缓存数据
        return postCommentVOList == null ? getPostCommentVOList(postId):postCommentVOList;
    }
}




