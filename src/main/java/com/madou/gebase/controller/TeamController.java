package com.madou.gebase.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.madou.gebase.common.BaseResponse;
import com.madou.gebase.common.ErrorCode;
import com.madou.gebase.common.ResultUtils;
import com.madou.gebase.model.User;
import com.madou.gebase.model.UserTeam;
import com.madou.gebase.model.dto.TeamQuery;
import com.madou.gebase.exception.BusinessException;
import com.madou.gebase.model.Team;
import com.madou.gebase.model.request.*;
import com.madou.gebase.model.vo.TeamVO;
import com.madou.gebase.model.vo.UserTeamVO;
import com.madou.gebase.service.TeamService;
import com.madou.gebase.service.UserService;
import com.madou.gebase.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 队伍接口
 *
 * @author MA_dou
 */
@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;

    /**
     * 创建队伍
     *
     * @param teamAddRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest httpServletRequest) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    /**
     * 解散队伍
     *
     * @param objectIdRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody ObjectIdRequest objectIdRequest, HttpServletRequest httpServletRequest) {
        if (objectIdRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = objectIdRequest.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 更新队伍
     *
     * @param teamUpdateRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest httpServletRequest) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 查询队伍
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<TeamVO> getTeamById(@RequestParam long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TeamVO team = teamService.getTeamInfoById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询队伍失败");
        }
        return ResultUtils.success(team);
    }

    /**
     * 查询队伍列表
     *
     * @param teamQuery
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<UserTeamVO>> listTeams(TeamQuery teamQuery, HttpServletRequest httpServletRequest) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(httpServletRequest);
        List<UserTeamVO> teamList = teamService.listTeams(teamQuery, isAdmin);


        //队伍id
        List<Long> teamIdList = teamList.stream().map(UserTeamVO::getId).collect(Collectors.toList());
        //用户加入队伍 hasJoin -> true
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(httpServletRequest);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            //已经加入队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
        }
        return ResultUtils.success(teamList);
    }

    /**
     * 查询队伍列表（分页）
     *
     * @param teamQuery
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest httpServletRequest) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 退出队伍
     *
     * @param teamQuitRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest httpServletRequest) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 我创建的队伍
     *
     * @param teamQuery
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<UserTeamVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest httpServletRequest) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(httpServletRequest);
        teamQuery.setUserId(loginUser.getId());
        List<UserTeamVO> teamList = teamService.listTeams(teamQuery, true);
        //队伍hasJoin字段位true
        teamList.forEach(team -> {
            team.setHasJoin(true);
        });
        return ResultUtils.success(teamList);
    }

    /**
     * 我加入的队伍
     *
     * @param teamQuery
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<UserTeamVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest httpServletRequest) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        //根据userId取出加入队伍teamId的值
        List<Long> longList = new ArrayList<>(listMap.keySet());
        teamQuery.setListUserId(longList);
        List<UserTeamVO> teamList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(longList)){
            teamList = teamService.listTeams(teamQuery, true);
            //队伍hasJoin字段位true
            teamList.forEach(team -> {
                team.setHasJoin(true);
            });
        }
        return ResultUtils.success(teamList);
    }

}
