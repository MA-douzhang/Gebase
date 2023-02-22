package com.madou.gebase.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.madou.gebase.common.ErrorCode;
import com.madou.gebase.exception.BusinessException;
import com.madou.gebase.model.Team;
import com.madou.gebase.model.UserTeam;
import com.madou.gebase.service.TeamService;
import com.madou.gebase.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 每天凌晨0点解散过期队伍
 *
 * @author yupi
 */
@Component
@Slf4j
public class timedTasksJob {

    @Resource
    private TeamService teamService;
    @Resource
    private UserTeamService userTeamService;



    // 每天执行，解散过期队伍
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOutTeam() {
        //移除所有队伍中用户
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("expireTime", new Date());
        //过期的队伍
        List<Team> teamList = teamService.list(queryWrapper);
        teamList.forEach(team -> {
            //删除过期队伍与用户的关系
            Long teamId = team.getId();
            boolean result = teamService.removeByTeamId(teamId);
            if (!result){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"定时任务错误");
            }
        });
    }

}
