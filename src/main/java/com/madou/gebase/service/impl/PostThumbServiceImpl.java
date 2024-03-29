package com.madou.gebase.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.madou.gebase.common.ErrorCode;
import com.madou.gebase.contant.RedisConstant;
import com.madou.gebase.exception.BusinessException;
import com.madou.gebase.mapper.PostThumbMapper;
import com.madou.gebase.model.Post;
import com.madou.gebase.model.PostThumb;
import com.madou.gebase.model.User;
import com.madou.gebase.service.PostService;
import com.madou.gebase.service.PostThumbService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author MA_dou
 * @description 针对表【post_thumb(帖子点赞)】的数据库操作Service实现
 * @createDate 2023-04-01 12:26:07
 */
@Service
@Slf4j
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb>
        implements PostThumbService {

    @Resource
    PostService postService;

    @Resource
    RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public int doPostThumb(long postId, User loginUser) {
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userId = loginUser.getId();
        //代理类，在同一类中非业务方法调用业务方法会让业务失效
        PostThumbService postThumbService = (PostThumbService) AopContext.currentProxy();
        // 锁要包裹事务方法
        RLock lock = redissonClient.getLock(RedisConstant.REDIS_THUMB_KEY + userId);
        try {
            while (true) {
                //线程重复拿锁
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    return postThumbService.doPostThumbInner(userId, postId);
                }
            }
        } catch (InterruptedException e) {
            log.error("get thumb key error", e);
            return 0;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    /**
     * 封装了事务的方法
     *
     * @param userId
     * @param postId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPostThumbInner(long userId, long postId) {
        PostThumb postThumb = new PostThumb();
        postThumb.setUserId(userId);
        postThumb.setPostId(postId);
        //查询缓存
        String redisPostThumbKey = RedisConstant.REDIS_POST_THUMB_KEY + postId;
        //查询该用户是否存在点赞缓存中
        Boolean isMember = redisTemplate.opsForSet().isMember(redisPostThumbKey, userId);
        boolean result;
        //点赞过 ，取消点赞
        if (BooleanUtils.isTrue(isMember)) {
            QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>(postThumb);
            //删除点赞表中信息
            result = this.remove(thumbQueryWrapper);
            if (result) {
                // 点赞数 - 1
                result = postService.update()
                        .eq("id", postId)
                        .gt("thumbNum", 0)
                        .setSql("thumbNum = thumbNum - 1")
                        .update();
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            //修改缓存
            if (result){
                Long remove = redisTemplate.opsForSet().remove(redisPostThumbKey, userId);
                if (remove == null || remove.equals(0L) ){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
            }
            return result ? -1 : 0;
            //未点赞
        } else {
            result = this.save(postThumb);
            if (result) {
                // 点赞数 + 1
                result = postService.update()
                        .eq("id", postId)
                        .setSql("thumbNum = thumbNum + 1")
                        .update();

            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            //存入缓存
            if (result){
                Long add = redisTemplate.opsForSet().add(redisPostThumbKey, userId);
                if (add == null || add.equals(0L) ){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
            }
            return result ? 1 : 0;
        }
    }


    @Override
    public List<Long> getUserPostThumb(User loginUser) {
        long userId = loginUser.getId();
        QueryWrapper<PostThumb> queryPostThumbWrapper = new QueryWrapper<>();
        queryPostThumbWrapper.eq("userId", userId);
        //查询出当前用户点赞所有帖子
        List<PostThumb> postThumbList = this.list(queryPostThumbWrapper);
        //给一个postId不存在的初始值
        List<Long> userPostThumb = Arrays.asList(-1L);
        //如果不为空，则存在点赞帖子
        if (postThumbList != null) {
            userPostThumb = postThumbList.stream().map(PostThumb::getPostId).collect(Collectors.toList());
        }
        return userPostThumb;
    }

    @Override
    public boolean isPostThumb(Long id, long userId) {
        Boolean member = redisTemplate.opsForSet().isMember(RedisConstant.REDIS_POST_THUMB_KEY + id, userId);
        return BooleanUtils.isTrue(member);
    }
}




