package com.bin.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bin.usercenter.Exception.BusinessException;
import com.bin.usercenter.common.BaseResponse;
import com.bin.usercenter.common.ErrorCode;
import com.bin.usercenter.common.ResultUtils;
import com.bin.usercenter.model.User;
import com.bin.usercenter.model.request.UserLoginRequest;
import com.bin.usercenter.model.request.UserRegiserRequest;
import com.bin.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.bin.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.bin.usercenter.contant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegiserRequest userRegiserRequest){
        if(userRegiserRequest == null){
            throw new BusinessException(ErrorCode.PARANMS_ERROR);
        }
        String userAccount = userRegiserRequest.getUserAccount();
        String userPassword = userRegiserRequest.getCheckPassword();
        String checkPassword = userRegiserRequest.getCheckPassword();
        String phone = userRegiserRequest.getPhone();
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,phone)){
            throw new BusinessException(ErrorCode.PARANMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword,phone);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if(request == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        int result =  userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrent(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long userId = currentUser.getId();
        //校验用户是否合法
        User orginUser = userService.getById(userId);
        User safeUser = userService.getSafetUser(orginUser);
        return ResultUtils.success(safeUser);

    }


    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username,HttpServletRequest request){
        //验证管理员权限
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNoneBlank(username)) {
            queryWrapper.like("username",username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletehUser(@RequestBody long id,HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARANMS_ERROR);
        }
        Boolean result = userService.removeById(id);
        return ResultUtils.success(result);
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

