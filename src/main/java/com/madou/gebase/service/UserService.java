package com.madou.gebase.service;

import com.madou.gebase.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author MA_dou
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2022-12-17 22:48:31
*/
public interface UserService extends IService<User> {
    /**
     *
     * @param userAccount 账号
     * @param userPassword 账号密码
     * @param checkPassword 密码校验
     * @return 返回用户id
     */
    long userRegister(String userAccount,String userPassword, String checkPassword);

    /**
     *
     * @param userAccount 账号
     * @param userPassword  密码
     * @param httpServletRequest 请求对象
     * @return 用户信息
     */
    User doLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest);

    /**
     * 用户脱敏
     * @param originUser 用户信息
     * @return 用户脱敏后信息
     */
    User getSafetyUser(User originUser);

    /**
     *   用户注销
     * @param httpServletRequest servlet对象
     * @return 1为退出成功
     */
    int userLogout(HttpServletRequest httpServletRequest);

    /**
     * 标签查询用户
     * @param tagNameList 标签列表
     * @return  符合标签的用户列表
     */
    List<User> searchUserByTags(List<String> tagNameList);

    /**
     * 查询登录用户信息
     * @param httpServletRequest
     * @return user
     */
    User getLoginUser(HttpServletRequest httpServletRequest);

    /**
     * 更新用户信息
     * @param user
     * @param loginUser
     * @return 1为更新成功，0为失败
     */
    int updateUser(User user, User loginUser);

    boolean isAdmin(HttpServletRequest httpServletRequest);

    boolean isAdmin(User loginUser);

    /**
     * 推荐用户（根据标签）
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);
}
