package com.madou.gebase.service;

import com.madou.gebase.model.TeamTask;
import com.baomidou.mybatisplus.extension.service.IService;
import com.madou.gebase.model.User;
import com.madou.gebase.model.request.TeamTaskCommentAddRequest;
import com.madou.gebase.model.vo.TeamTaskVO;

import java.util.List;

/**
* @author MA
* @description 针对表【team_task(队伍任务)】的数据库操作Service
* @createDate 2025-04-19 12:03:18
*/
public interface TeamTaskService extends IService<TeamTask> {

    /**
     * 添加队伍任务
     * @param teamTask
     * @param loginUser
     * @return
     */
    Long addTeamTask(TeamTask teamTask, User loginUser);

    /**
     * 删除队伍任务
     * @param id
     * @param loginUser
     * @return
     */
    Boolean deleteTeamTask(Long id, User loginUser);

    /**
     * 根据队伍任务id查询详情
     * @param id
     * @return
     */
    TeamTaskVO getTeamTaskInfoById(Long id);

    /**
     * 添加队伍任务评论
     * @param teamTaskCommentAddRequest
     * @param loginUser
     * @return
     */
    Boolean addComment(TeamTaskCommentAddRequest teamTaskCommentAddRequest, User loginUser);

    /**
     * 删除队伍任务评论
     * @param id
     * @param loginUser
     * @return
     */
    Boolean deleteComment(Long id, User loginUser);

    /**
     * 根据队伍id查询队伍任务信息
     * @param teamId
     * @return
     */
    List<TeamTaskVO> getTeamTaskInfoByTeamId(Long teamId);

    /**
     * 完成任务
     * @param id
     * @param loginUser
     * @return
     */
    boolean finishTask(Long id, User loginUser);
}
