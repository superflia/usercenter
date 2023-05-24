package com.bin.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.usercenter.service.UserService;
import com.bin.usercenter.model.User;
import com.bin.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bin.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
* @author Bin
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-05-22 21:03:49
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    private static final String SALT = "ruyi";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //校验
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return -1;
        }
        if(userAccount.length() < 4){
            return -1;
        }
        if (userPassword.length() <8 || checkPassword.length() <8){
            return -1;
        }

        //账户不包含特殊字符
        String vaildPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(vaildPattern).matcher(userAccount);
        if (matcher.find()){
            return -1;
        }

        //密码和校验密码相同
        if (!userPassword.equals(checkPassword)){
            return -1;
        }

        //账户不重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account",userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0){
            return -1;
        }

        //密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+ userPassword).getBytes());

        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if(!saveResult){
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //校验
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            return null;
        }
        if(userAccount.length() < 4){
            return null;
        }
        if (userPassword.length() <8 ){
            return null;
        }

        //账户不包含特殊字符
        String vaildPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(vaildPattern).matcher(userAccount);
        if (matcher.find()){
            return null;
        }

        //密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+ userPassword).getBytes());

        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account",userAccount);
        queryWrapper.eq("user_password",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null){
            log.info("user login failed,userAccount cannot match userPassword");
            return null;
        }

        //用户脱敏
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setAvatarurl(user.getAvatarurl());
        safeUser.setGender(user.getGender());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setRole(user.getRole());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setUserStatus(0);
        safeUser.setCreateTime(user.getCreateTime());

        //记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,safeUser);

        return safeUser;
    }
}




