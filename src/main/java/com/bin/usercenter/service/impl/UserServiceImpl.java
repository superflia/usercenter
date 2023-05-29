package com.bin.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bin.usercenter.Exception.BusinessException;
import com.bin.usercenter.common.ErrorCode;
import com.bin.usercenter.common.ResultUtils;
import com.bin.usercenter.service.UserService;
import com.bin.usercenter.model.User;
import com.bin.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    public long userRegister(String userAccount, String userPassword, String checkPassword,String phone) {
        //校验
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,phone)){
            throw new BusinessException(ErrorCode.PARANMS_ERROR,"参数为空");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARANMS_ERROR,"用户帐号过短");
        }
        if (userPassword.length() <8 || checkPassword.length() <8){
            throw new BusinessException(ErrorCode.PARANMS_ERROR,"用户密码过短");
        }
        if(phone.length() < 11){
            throw new BusinessException(ErrorCode.PARANMS_ERROR,"用户电话过短");
        }

        //账户不包含特殊字符
        String vaildPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(vaildPattern).matcher(userAccount);
        if (matcher.find()){
            return -1;
        }

        //密码和校验密码相同
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARANMS_ERROR,"密码和校验密码不同");
        }

        //账户不重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account",userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARANMS_ERROR,"用户已注册");
        }

        //注册手机号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone",phone);
        long count1 = userMapper.selectCount(queryWrapper);
        if(count1 > 0){
            throw new BusinessException(ErrorCode.PARANMS_ERROR,"此电话已注册");
        }

        //密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+ userPassword).getBytes());

        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPhone(phone);
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
            throw new BusinessException(ErrorCode.PARANMS_ERROR,"参数为空");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARANMS_ERROR,"用户帐号过短");
        }
        if (userPassword.length() <8 ){
            throw new BusinessException(ErrorCode.PARANMS_ERROR,"用户密码过短");
        }

        //账户不包含特殊字符
        String vaildPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(vaildPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARANMS_ERROR,"账户包含特殊字符");
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
            throw new BusinessException(ErrorCode.PARANMS_ERROR,"用户不存在");
        }
        //用户脱敏
        User safeUser = getSafetUser(user);


        //记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE,safeUser);

        return safeUser;

    }

    /**
     * 用户脱敏
     * @param orginuser 用户信息
     * @return 脱敏用户信息
     */
    @Override
    public User getSafetUser(User orginuser){
        if (orginuser == null){
            return null;
        }
        User safeUser = new User();
        safeUser.setId(orginuser.getId());
        safeUser.setUsername(orginuser.getUsername());
        safeUser.setAvatarurl(orginuser.getAvatarurl());
        safeUser.setGender(orginuser.getGender());
        safeUser.setUserAccount(orginuser.getUserAccount());
        safeUser.setRole(orginuser.getRole());
        safeUser.setPhone(orginuser.getPhone());
        safeUser.setEmail(orginuser.getEmail());
        safeUser.setUserStatus(0);
        safeUser.setCreateTime(orginuser.getCreateTime());
        return safeUser;
    }

    /**
     * 用户注销
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

}




