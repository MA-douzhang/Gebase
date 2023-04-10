package com.madou.gebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.madou.gebase.common.ErrorCode;
import com.madou.gebase.contant.RedisConstant;
import com.madou.gebase.exception.BusinessException;
import com.madou.gebase.mapper.TeamMapper;
import com.madou.gebase.model.Team;
import com.madou.gebase.model.User;
import com.madou.gebase.model.UserTeam;
import com.madou.gebase.model.dto.TeamQuery;
import com.madou.gebase.model.enums.TeamStatusEnums;
import com.madou.gebase.model.request.TeamJoinRequest;
import com.madou.gebase.model.request.TeamQuitRequest;
import com.madou.gebase.model.request.TeamUpdateRequest;
import com.madou.gebase.model.vo.TeamVO;
import com.madou.gebase.model.vo.UserTeamVO;
import com.madou.gebase.model.vo.UserVO;
import com.madou.gebase.service.TeamService;
import com.madou.gebase.service.UserService;
import com.madou.gebase.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author MA_dou
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2023-01-31 14:09:19
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {
    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {


        //请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //用户是否登录
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }
        final long userId = loginUser.getId();
        //队伍最大人数不大于20不小于1
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //队伍名称 <=20
        String teamName = team.getTeamName();
        if (StringUtils.isNotBlank(teamName) && teamName.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名不满足要求");
        }
        //描述小于512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不满足要求");
        }
        // teamStatue 0为公开 默认为0
        int statue = Optional.ofNullable(team.getTeamState()).orElse(0);
        TeamStatusEnums teamStatusEnums = TeamStatusEnums.getEnumByValues(statue);
        if (teamStatusEnums == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        // teamStatue 为加密状态 需要密码，密码<=32
        String teamPassword = team.getTeamPassword();
        if (TeamStatusEnums.SECRET.equals(teamStatusEnums) && (StringUtils.isBlank(teamPassword) || teamPassword.length() > 32)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍加密不满足要求");
        }
        //超时时间大于当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间小于当前时间");
        }
        //校验用户最多创建5个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long teamCount = this.count(queryWrapper);
        if (teamCount >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍数量不能大于5");
        }
        //信息插入队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //信息插入队伍关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<UserTeamVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> listUserId = teamQuery.getListUserId();
            if (!CollectionUtils.isEmpty(listUserId)) {
                queryWrapper.in("id", listUserId);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("teamName", searchText).or().like("description", searchText));
            }
            String teamName = teamQuery.getTeamName();
            if (StringUtils.isNotBlank(teamName)) {
                queryWrapper.like("teamName", teamName);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum <= 5) {
                queryWrapper.like("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            Integer teamState = teamQuery.getTeamState();
            TeamStatusEnums enumByValues = TeamStatusEnums.getEnumByValues(teamState);
            if (enumByValues == null) {
                //为空 默认公开
                enumByValues = TeamStatusEnums.PUBLIC;
            }
            if (!isAdmin && userId == null && enumByValues.equals(TeamStatusEnums.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_ADMIN);
            }
            queryWrapper.eq("teamState", enumByValues.getValue());

        }
        //不展示过期队伍
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }

        List<UserTeamVO> userTeamVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            User user = userService.getById(userId);
            UserTeamVO userTeamVO = new UserTeamVO();
            //脱敏信息
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                userTeamVO.setCreateUser(userVO);
            }
            BeanUtils.copyProperties(team, userTeamVO);
            userTeamVOList.add(userTeamVO);
        }
        //队伍id
        List<Long> teamIdList = userTeamVOList.stream().map(UserTeamVO::getId).collect(Collectors.toList());
        //加入队伍人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        //队伍Id => 加入这个队伍的用户
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        userTeamVOList.forEach(team -> {
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
        });
        return userTeamVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新数据不能为空");
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //只要管理员和队长才能修改
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        TeamStatusEnums teamStatusEnums = TeamStatusEnums.getEnumByValues(teamUpdateRequest.getTeamState());
        if (teamStatusEnums.equals(TeamStatusEnums.SECRET)) {
            if (StringUtils.isBlank(teamUpdateRequest.getTeamPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间需要设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍已过期");
        }
        TeamStatusEnums teamStatusEnums = TeamStatusEnums.getEnumByValues(team.getTeamState());
        if (teamStatusEnums.equals(TeamStatusEnums.PRIVATE)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "不能加入私有队伍");
        }
        String teamPassword = teamJoinRequest.getTeamPassword();
        if (TeamStatusEnums.SECRET.equals(teamStatusEnums)) {
            if (StringUtils.isBlank(teamPassword) || !team.getTeamPassword().equals(teamPassword)) {
                throw new BusinessException(ErrorCode.NULL_ERROR, "密码错误");
            }
        }

        //该用户加入队伍数量
        long userId = loginUser.getId();
        //只有一个线程抢到加入该队伍的锁
        RLock lock = redissonClient.getLock(RedisConstant.REDIS_JOIN_TEAM_KEY + teamId);
        try {
            //重复抢锁
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)){
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNum > 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多加入或者创建5个队伍");
                    }

                    //不能重复加入队伍
                    userTeamQueryWrapper.eq("teamId", teamId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ErrorCode.NULL_ERROR, "用户已加入该队伍");
                    }

                    //已经加入队伍数量
                    long hasTeamJoinNum = getHasTeamJoinTeam(teamId);
                    if (hasTeamJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.NULL_ERROR, "队伍已满");
                    }
                    //修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCache joinTeam error", e);
            return false;
        }finally {
            //释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("teamId", teamId);
        long hasJoinNum = userTeamService.count(queryWrapper);
        if (hasJoinNum == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入该队伍");
        }
        long hasUserJoinTeam = getHasTeamJoinTeam(teamId);
        //只剩一人删除队伍 队长退出，队长顺位
        if (hasUserJoinTeam == 1) {
            //删除队伍和队伍关系
            this.removeById(teamId);
        } else {
            //队长退出
            if (team.getUserId() == userId) {
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("teamId", teamId);
                queryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) && userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                //删除队长关系
                UserTeam nextTeam = userTeamList.get(1);
                Long nextUserId = nextTeam.getUserId();
                Team updateTeam = new Team();
                updateTeam.setUserId(nextUserId);
                updateTeam.setId(teamId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍队长更新失败");
                }
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("teamId", teamId);
                queryWrapper.eq("userId", userId);
            }
        }
        //移除关系
        return userTeamService.remove(queryWrapper);
    }

    @Override
    public boolean deleteTeam(long teamId, User loginUser) {
        Team team = getTeamById(teamId);
        //是否为队长
        long userId = loginUser.getId();
        if (team.getUserId() != userId) {
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        //移除队伍
        return removeByTeamId(teamId);
    }

    @Override
    public TeamVO getTeamInfoById(long id) {
        Team team = this.getById(id);
        Long userId = team.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User createUser = userService.getById(userId);
        TeamVO teamVO = new TeamVO();
        //脱敏信息
        if (createUser != null) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(createUser, userVO);
            teamVO.setCreateUser(userVO);
        }
        BeanUtils.copyProperties(team, teamVO);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", id);
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        //获取加入队伍的用户id
        List<Long> userIdList = userTeamList.stream().map(UserTeam::getUserId).collect(Collectors.toList());
        //根据用户id查出详细的信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        List<User> userList = userService.list(userQueryWrapper);
        //用户脱敏
        List<UserVO> userVOList = new ArrayList<>();
        for (User user : userList) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            userVOList.add(userVO);
        }
        teamVO.setUserJoinList(userVOList);
        teamVO.setHasJoinNum(userVOList.size());
        return teamVO;
    }


    /**
     * 获取队伍中的人数
     *
     * @param teamId
     * @return
     */
    private long getHasTeamJoinTeam(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    /**
     * 根据队伍teamId获取队伍信息
     *
     * @param teamId
     * @return
     */
    private Team getTeamById(long teamId) {
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 根据队伍id解散队伍
     *
     * @param teamId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByTeamId(long teamId) {
        QueryWrapper<UserTeam> teamIdQueryWrapper = new QueryWrapper<>();
        teamIdQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(teamIdQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍中用户失败");
        }
        return this.removeById(teamId);
    }
}




