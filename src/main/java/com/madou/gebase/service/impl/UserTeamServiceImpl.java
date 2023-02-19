package com.madou.gebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.madou.gebase.mapper.UserTeamMapper;
import com.madou.gebase.model.vo.UserTeamVO;
import com.madou.gebase.service.UserTeamService;
import com.madou.gebase.model.UserTeam;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author MA_dou
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-01-31 14:10:01
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {



}




