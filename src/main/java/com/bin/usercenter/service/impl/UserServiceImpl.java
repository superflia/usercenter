package com.bin.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.usercenter.model.User;
import com.bin.usercenter.service.UserService;
import generator.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author Bin
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-05-22 21:00:21
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

}




