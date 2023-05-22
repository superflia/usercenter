package com.bin.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bin.usercenter.model.User;

/**
* @author Bin
* @description 针对表【user】的数据库操作Service
* @createDate 2023-05-22 21:03:49
*/
public interface UserService extends IService<User> {
    /**
     *
     * @param useAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);
}
