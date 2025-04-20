package com.madou.gebase.service;

import com.madou.gebase.model.TeamTaskComment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.madou.gebase.model.vo.TeamTaskCommentVO;

import java.util.List;

/**
* @author MA
* @description 针对表【team_task_comment(队伍任务评论)】的数据库操作Service
* @createDate 2025-04-19 12:03:46
*/
public interface TeamTaskCommentService extends IService<TeamTaskComment> {

    /**
     * 查询队伍任务评论
     * @param teamTaskVOId
     * @return
     */
    List<TeamTaskCommentVO> getTeamTaskCommentVOList(Long teamTaskVOId);
}
