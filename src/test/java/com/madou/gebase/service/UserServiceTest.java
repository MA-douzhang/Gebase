package com.madou.gebase.service;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.madou.gebase.model.User;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser(){
        User user = new User();
        user.setUsername("MA_dou");
        user.setUserAccount("123");
        user.setAvatarUrl("baidu.com");
        user.setGender(0);
        user.setUserPassword("123");
        user.setPhone("123456789");
        user.setEmail("84183856@qq.com");
        boolean save = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(save);
    }

    @Test
    void userRegister() {
        //相同账号
        String userAccount = "MA_dou1";
        String userPassword = "123456789";
        String checkPassword = "123456789";
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(1,result);

//        //密码为空
//        userAccount = "MA_dou1";
//        userPassword = "";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1,result);
//        //账号不含特殊字符
//        userAccount = "MA dou1";
//        userPassword = "123456789";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1,result);
//
//        userAccount = "MAdou";
//        userPassword = "123456789";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1,result);
    }

    @Test
    void testSearchUserByTags(){
        List<String> tagNameList = Arrays.asList("java","python");
        List<User> users = userService.searchUserByTags(tagNameList);
        Assert.assertNotNull(users);
    }
}
