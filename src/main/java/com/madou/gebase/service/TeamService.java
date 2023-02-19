package com.madou.gebase.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.madou.gebase.model.Team;
import com.madou.gebase.model.User;
import com.madou.gebase.model.dto.TeamQuery;
import com.madou.gebase.model.request.TeamAddRequest;
import com.madou.gebase.model.request.TeamJoinRequest;
import com.madou.gebase.model.request.TeamQuitRequest;
import com.madou.gebase.model.request.TeamUpdateRequest;
import com.madou.gebase.model.vo.UserTeamVO;

import java.util.List;

/**
* @author MA_dou
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-01-31 14:09:19
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 字段检索队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<UserTeamVO> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 更新队伍信息
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 队长删除队伍
     * @param teamId
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long teamId, User loginUser);
}
