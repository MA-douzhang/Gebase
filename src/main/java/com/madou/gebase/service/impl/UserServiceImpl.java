package com.madou.gebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.madou.gebase.common.ErrorCode;
import com.madou.gebase.contant.UserConstant;
import com.madou.gebase.exception.BusinessException;
import com.madou.gebase.model.User;
import com.madou.gebase.service.UserService;
import com.madou.gebase.mapper.UserMapper;
import com.madou.gebase.utils.AlgorithmUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.madou.gebase.contant.UserConstant.ADMIN_ROLE;
import static com.madou.gebase.contant.UserConstant.USER_LOGIN_STATE;

/**
 * @author MA_dou
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2022-12-17 22:48:31
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    UserMapper userMapper;
    /**
      * 盐值
     */
    private static final String SALT = "ma_dou";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //账号，密码 不能为空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码为空");
        }
        // 账号校验
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度小于4");
        }
        // 密码校验
        if (userPassword.length()<8 || checkPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度小于8");
        }
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"校验密码不一致");
        }
        //账号非法字符校验
        String validPattern = "[\\u4e00-\\u9fa5\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号含非法字符");
        }
        //账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count = this.count(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号重复");
        }
        //加密

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"数据插入错误");
        }
        Long id = user.getId();
        return id;
    }

    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest) {
        //账号，密码 不能为空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"登录参数为空");
        }
        // 账号校验
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度小于4");
        }
        //账号非法字符校验
        String validPattern = "[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号非法");
        }
        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());

        //账号查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);

        //用户不存在
        if (user == null){
            log.info("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或者密码错误");
        }
        User safetyUser = getSafetyUser(user);
        //记录用户登录态
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        return safetyUser;
    }
    @Override
    public User getSafetyUser(User originUser){
        if(originUser == null) return null;
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserProfile(originUser.getUserProfile());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserState(originUser.getUserState());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest httpServletRequest) {
        httpServletRequest.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
    /**
     * 根据标签查询用户SQL版
     * @param tagNameList 标签列表
     * @return
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //查询出所有的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //在内存中查询符合要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String>tempTagNameStr = gson.fromJson(tagsStr,new TypeToken<Set<String>>(){}.getType());
            //是否为空，为空返回HashSet的默认值，否则返回数值
            tempTagNameStr = Optional.ofNullable(tempTagNameStr).orElse(new HashSet<>());
            //返回false会过滤掉
            for (String tagName:tagNameList
                 ) {
                if(!tempTagNameStr.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 查询登录用户信息
     * @param httpServletRequest http请求的封装响应信息
     * @return user
     */
    @Override
    public User getLoginUser(HttpServletRequest httpServletRequest) {
        if(httpServletRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = (User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        if(user == null){
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }
        return user;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        //todo 对传递来的user参数判断，不能为空等等，增加判断条件
        if(userId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //如果是管理员可以改任何人的信息
        //本人可以改自己的信息
        if(!isAdmin(loginUser) && loginUser.getId() != userId){
            throw new BusinessException(ErrorCode.NO_ADMIN);
        }
        User oldUser = userMapper.selectById(userId);
        if(oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     * 根据标签查询用户SQL版
     * @param tagNameList 标签列表
     * @return
     */
    @Deprecated
    public List<User> searchUserByTagsSQL(List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //模糊查询 %java%
        for (String tagName:tagNameList
             ) {
           queryWrapper = queryWrapper.like("tags",tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 查询是否为管理员
     * @param httpServletRequest 请求头封装信息
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest httpServletRequest) {
        Object userObj = httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user == null) {
            return false;
        }
        return user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 查询是否为管理员
     * @param loginUser 用户信息
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        if (loginUser == null) {
            return false;
        }
        return loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 匹配推荐用户 （根据标签）
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","tags");
        List<User> userList = this.list(queryWrapper);
        String loginUserTags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> loginTagList= gson.fromJson(loginUserTags,new TypeToken<List<String>>(){}.getType());
        //用户列表下标 相似度
        List<Pair<User,Long>> tagList = new ArrayList<>();
        for (User user : userList) {
            long userId = user.getId();
            String tags = user.getTags();
            //计算出所有用户除开空标签和自己
            if (StringUtils.isBlank(tags) || userId == loginUser.getId()){
                continue;
            }
            List<String> userTagList= gson.fromJson(tags,new TypeToken<List<String>>(){}.getType());
            //计算分数
            long distance = AlgorithmUtils.minDistance(loginTagList, userTagList);
            tagList.add(new Pair<>(user,distance));
        }
        //按分数 大到小排序
        List<Pair<User, Long>> topUserPairList = tagList.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        //原本顺序的userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",userIdList);
        Map<Long,List<User>> userIdUserListMap = this.list(queryWrapper)
                .stream().map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId: userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }
}




