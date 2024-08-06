package com.lhf.usercenter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhf.usercenter.common.BaseResponse;
import com.lhf.usercenter.common.ErrorCode;
import com.lhf.usercenter.common.utils.ResultUtil;
import com.lhf.usercenter.exception.BusinessException;
import com.lhf.usercenter.model.User;
import com.lhf.usercenter.model.requestion.UserLoginRequest;
import com.lhf.usercenter.model.requestion.UserRegisterRequest;
import com.lhf.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.lhf.usercenter.contant.UserContant.USER_LOGIN_STATUS;

@Api("用户模块")
@CrossOrigin(value = "http://localhost:3000/", allowCredentials = "true")
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @ApiOperation("用户注册")
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
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");

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

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User user = (User) userObj;
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        // 校验
        User currentUser = userService.getCurrentUser(request);
        // 脱敏
        User safetyUser = userService.getSafetyUser(currentUser);
        return ResultUtil.success(safetyUser);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 校验
        User loginUser = userService.getLoginUser(request);
        // 更新
        int result = userService.updateUser(user, loginUser);
        return ResultUtil.success(result);
    }

    @PostMapping("/serarch/tag")
    public BaseResponse searchUserByTagsName(@RequestBody List<String> tagNameList) {
        if (tagNameList == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");

        }
        return ResultUtil.success(userService.searchUserByTagsName(tagNameList));
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageNum, long pageSize, HttpServletRequest request) {
        if (pageNum <= 0 || pageSize <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数为空");
        }
        // 获取当前页数据
        Page<User> page = userService.page(new Page<>(pageNum, pageSize));
        List<User> userList = page.getRecords();
        // 脱敏
        List<User> safetyUserList = userList.stream().map(userService::getSafetyUser).collect(Collectors.toList());
        page.setRecords(safetyUserList);
        //返回脱敏后的数据
        return ResultUtil.success(page);
    }

}