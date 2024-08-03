package com.lhf.usercenter.controller;

import com.lhf.usercenter.common.BaseResponse;
import com.lhf.usercenter.common.ErrorCode;
import com.lhf.usercenter.common.utils.ResultUtil;
import com.lhf.usercenter.exception.BusinessException;
import com.lhf.usercenter.model.User;
import com.lhf.usercenter.model.requestion.UserLoginRequest;
import com.lhf.usercenter.model.requestion.UserRegisterRequest;
import com.lhf.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "部分请求参数为空");
        }
        boolean result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtil.success(result);
    }

    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");

        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "部分请求参数为空");

        }
        return userService.userLogin(userAccount, userPassword, request);
    }

    @GetMapping("/search")
    public BaseResponse<User> searchUserByUserName(String userAccount, HttpServletRequest request) {
        if (StringUtils.isBlank(userAccount)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        User user = userService.searchUserByUserName(userAccount, request);
        return ResultUtil.success(user);
    }

    @PostMapping("/delete")
    public BaseResponse deleteUser(@RequestBody Long id, HttpServletRequest request) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        boolean result = userService.deleteUser(id, request);
        return ResultUtil.success(result);
    }

    @PostMapping("/logout")
    public BaseResponse userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");

        }
        boolean result = userService.userLogout(request);
        return ResultUtil.success(result);
    }
}