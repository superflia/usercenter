package com.bin.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bin.usercenter.model.User;
import com.bin.usercenter.model.request.UserLoginrRequest;
import com.bin.usercenter.model.request.UserRegiserRequest;
import com.bin.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import static com.bin.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.bin.usercenter.contant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegiserRequest userRegiserRequest){
        if(userRegiserRequest == null){
            return null;
        }
        String userAccount = userRegiserRequest.getUserAccount();
        String userPassword = userRegiserRequest.getCheckPassword();
        String checkPassword = userRegiserRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return null;
        }
        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginrRequest userLoginrRequest , HttpServletRequest request){
        if(userLoginrRequest == null){
            return null;
        }
        String userAccount = userLoginrRequest.getUserAccount();
        String userPassword = userLoginrRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            return null;
        }
        return userService.userLogin(userAccount, userPassword, request);
    }

    @GetMapping("/search")
    public List<User> searchUsers(String username,HttpServletRequest request){
        //验证管理员权限
        if(!isAdmin(request)){
            return new ArrayList<>();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNoneBlank(username)) {
            queryWrapper.like("username",username);
        }
        return userService.list(queryWrapper);
    }

    @PostMapping("/delete")
    public boolean deletehUser(@RequestBody long id,HttpServletRequest request){
        if(!isAdmin(request)){
            return false;
        }
        if(id <= 0){
            return false;
        }
        return userService.removeById(id);
    }

    /**
     * 验证管理员权限
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getRole() == ADMIN_ROLE;
    }
}

