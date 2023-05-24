package com.bin.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.usercenter.model.User;

import javax.servlet.http.HttpServletRequest;

/**
* @author Bin
* @description 针对表【user】的数据库操作Service
* @createDate 2023-05-22 21:03:49
*/
public interface UserService extends IService<User> {
    /**
     *
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     *
     * @param userAccount 登录账号
     * @param userPassword 登录密码
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount,String userPassword,HttpServletRequest request);
}
