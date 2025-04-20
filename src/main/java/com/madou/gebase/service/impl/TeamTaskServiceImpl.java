package com.madou.gebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.madou.gebase.common.ErrorCode;
import com.madou.gebase.contant.RedisConstant;
import com.madou.gebase.exception.BusinessException;
import com.madou.gebase.model.*;
import com.madou.gebase.model.TeamTask;
import com.madou.gebase.model.request.TeamTaskCommentAddRequest;
import com.madou.gebase.model.vo.TeamTaskCommentVO;
import com.madou.gebase.model.vo.TeamTaskVO;
import com.madou.gebase.model.vo.TeamTaskCommentVO;
import com.madou.gebase.model.vo.TeamTaskVO;
import com.madou.gebase.service.NoticeService;
import com.madou.gebase.service.TeamTaskCommentService;
import com.madou.gebase.service.TeamTaskService;
import com.madou.gebase.mapper.TeamTaskMapper;
import com.madou.gebase.service.UserService;
import com.madou.gebase.utils.BeanConversionUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
* @author MA
* @description 针对表【team_task(队伍任务)】的数据库操作Service实现
* @createDate 2025-04-19 12:03:18
*/
@Service
public class TeamTaskServiceImpl extends ServiceImpl<TeamTaskMapper, TeamTask>
    implements TeamTaskService{
    @Resource
    private UserService userService;

    @Resource
    private TeamTaskCommentService teamTaskCommentService;
    @Resource
    private RedissonClient redissonClient;

    @Resource
    private NoticeService noticeService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Override
    public Long addTeamTask(TeamTask teamTask, User loginUser) {
        //请求参数是否为空
        if (ObjectUtils.isEmpty(teamTask)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //用户是否登录
        if (ObjectUtils.isEmpty(loginUser)) {
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }
        final long userId = loginUser.getId();
        //队伍描述
        String content = teamTask.getDescription();
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容为空");
        }
        if (content.length() < 10 || content.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容字数不符合要求");
        }
        //todo 判断敏感字符
        //信息插入队伍任务表
        teamTask.setId(null);
        teamTask.setUserId(userId);
        boolean result = this.save(teamTask);
        Long teamTaskId = teamTask.getId();
        if (!result || teamTaskId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "发布队伍任务失败");
        }
        return teamTaskId;

    }

    @Override
    public Boolean deleteTeamTask(Long id, User loginUser) {
        TeamTask teamTask = this.getById(id);
        if (teamTask == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍任务不存在");
        }
        //是否为队伍任务创建人或者管理员
        Long userId = teamTask.getUserId();
        if (!userService.isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        QueryWrapper<TeamTaskComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamTaskId", id);
        //删除队伍任务的评论。如果没有评论则不删除
        long teamTaskCommentCount = teamTaskCommentService.count(queryWrapper);
        if (teamTaskCommentCount > 0){
            boolean remove = teamTaskCommentService.remove(queryWrapper);
            if (!remove) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍任务评论失败");
            }
        }
        return this.removeById(id);
        
    }

    @Override
    public TeamTaskVO getTeamTaskInfoById(Long id) {
        TeamTask teamTask = this.getById(id);
        if (teamTask == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍任务不存在");
        }
        //队伍任务脱敏
        TeamTaskVO teamTaskVO = new TeamTaskVO();
        BeanUtils.copyProperties(teamTask, teamTaskVO);
        //查询队伍任务评论
        Long teamTaskVOId = teamTaskVO.getId();
        //查询队伍任务评论
        List<TeamTaskCommentVO> teamTaskCommentVOList = teamTaskCommentService.getTeamTaskCommentVOList(teamTaskVOId);
        teamTaskVO.setTeamTaskCommentList(teamTaskCommentVOList);
        return teamTaskVO;

    }

    @Override
    public Boolean addComment(TeamTaskCommentAddRequest teamTaskCommentAddRequest, User loginUser) {

        if (teamTaskCommentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //队伍任务id存在
        Long teamTaskId = teamTaskCommentAddRequest.getTeamTaskId();
        if (teamTaskId == null || teamTaskId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍任务不存在");
        }
        //内容字数小于200，内容不能为空
        String content = teamTaskCommentAddRequest.getContent();
        if (StringUtils.isBlank(content) || content.length() == 0 || content.length() >= 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容字数不符合要求");
        }
        //判断评论的pid,pid为null代表该条评论pid是队伍任务的创建者，反之是回复者的id
        TeamTaskComment teamTaskComment = new TeamTaskComment();
        BeanUtils.copyProperties(teamTaskCommentAddRequest, teamTaskComment);
        //获取队伍任务的创建者id
        TeamTask teamTask = this.getById(teamTaskId);
        if (teamTask == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍任务不存在");
        }
        final long userId = loginUser.getId();
        teamTaskComment.setUserId(userId);
        //查询当前队伍任务的评论缓存
        List<TeamTaskCommentVO> commentVOListCache = teamTaskCommentService.getTeamTaskCommentVOList(teamTaskId);
        //抢锁更新数据库评论和更新缓存
        RLock lock = redissonClient.getLock(RedisConstant.REDIS_POST_COMMENT_UPDATE_KEY);
        try {
            while (true){
                //反复抢锁，保证数据一致性
                if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                    //当前获得锁的线程的id是
                    System.out.println("getLock"+Thread.currentThread().getId());
                    //保存评论
                    boolean result = teamTaskCommentService.save(teamTaskComment);
                    //脱敏
                    TeamTaskCommentVO teamTaskCommentVO = new TeamTaskCommentVO();
                    BeanUtils.copyProperties(teamTaskComment,teamTaskCommentVO);
                    //添加默认值
                    teamTaskCommentVO.setAvatarUrl(loginUser.getAvatarUrl());
                    teamTaskCommentVO.setUsername(loginUser.getUsername());
                    //向评论列表添加评论
                    commentVOListCache.add(teamTaskCommentVO);


                    //发送通知给队伍任务的创建者
                    Notice notice = new Notice();
                    notice.setSenderId(userId);
                    notice.setReceiverId(teamTask.getUserId());
                    notice.setTargetId(teamTaskId);
                    notice.setContent(teamTaskComment.getContent());
                    //5为队伍任务评论
                    notice.setContentType(5);
                    long addNotice = noticeService.addNotice(notice);

                    if (addNotice<0) throw new BusinessException(ErrorCode.SYSTEM_ERROR,"通知失败");
                    //更新缓存
                    if (result){
                        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                        try {
                            valueOperations.set(RedisConstant.REDIS_POST_COMMENT_KEY+teamTaskId,commentVOListCache);
                        } catch (Exception e) {
                            log.error("redis set key error",e);
                        }
                        return true;
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("addComment error", e);
            return false;
        }finally {
            //只能自己释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    @Override
    public Boolean deleteComment(Long id, User loginUser) {
        TeamTaskComment teamTaskComment = teamTaskCommentService.getById(id);
        if (teamTaskComment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }
        Long userId = teamTaskComment.getUserId();
        Long teamTaskId = teamTaskComment.getTeamTaskId();
        TeamTask teamTask = this.getById(teamTaskId);
        //是否为评论的创建人或者管理员，帖子的创建者
        if (!userService.isAdmin(loginUser) && userId != loginUser.getId() && teamTask.getUserId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        return teamTaskCommentService.removeById(id);
    }

    @Override
    public List<TeamTaskVO> getTeamTaskInfoByTeamId(Long teamId) {
        QueryWrapper<TeamTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId",teamId);
        queryWrapper.orderByAsc("createTime");
        List<TeamTask> list = this.list(queryWrapper);
        List<TeamTaskVO> teamTaskVOList = BeanConversionUtil.convertList(list, TeamTaskVO.class);
        return teamTaskVOList;

    }

    @Override
    public boolean finishTask(Long id, User loginUser) {
        TeamTask teamTask = this.getById(id);
        teamTask.setTaskState(1);//完成任务
        teamTask.setEndTime(new Date());
        return this.updateById(teamTask);
    }
}




