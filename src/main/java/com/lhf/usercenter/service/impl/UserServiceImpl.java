package com.lhf.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lhf.usercenter.common.ErrorCode;
import com.lhf.usercenter.exception.BusinessException;
import com.lhf.usercenter.model.User;
import com.lhf.usercenter.service.UserService;
import com.lhf.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.lhf.usercenter.contant.UserContant.*;

/**
 * @author LHF
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2024-07-31 14:54:54
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;

    public static final String SALT = "lhf";

    @Override
    public boolean userRegister(String userAccount, String userPassword, String checkPassword) {
        //校验参数是否合法
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号长度过短");

        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度过短");

        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次输入的密码不匹配");

        }
        //封装用户信息
        User user = new User();
        user.setUserAccount(userAccount);
        //加密密码
        String handledPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        user.setUserPassword(handledPassword);

        //用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount); //查询条件
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            log.info("userAccount already exist");
            return false;
        }

        //插入数据库
        boolean result = this.save(user);
        if (!result) {
            log.info("userRegister fail");
            return false;
        }
        return result;
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //校验参数是否合法
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号长度过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度过短");
        }
        //封装用户信息
        User user = new User();
        user.setUserAccount(userAccount);
        //加密密码
        String handledPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        user.setUserPassword(handledPassword);

        //用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount); //查询条件
        queryWrapper.eq("userPassword", handledPassword); //查询条件
        user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("userAccount or userPassword is wrong");
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        //脱敏用户信息
        User safelyUser = getSafetyUser(user);

        //将用户信息存入session
        request.getSession().setAttribute(USER_LOGIN_STATUS, safelyUser);
        return safelyUser;
    }

    /**
     * 根据用户账号名查询用户信息
     *
     * @param userAccount 用户账号
     * @param request     请求
     * @return 用户信息
     */
    @Override
    public User searchUserByUserName(String userAccount, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.AUTH_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("userAccount", userAccount);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 删除用户
     *
     * @param id      用户id
     * @param request 请求
     * @return 是否删除成功
     */
    @Override
    public boolean deleteUser(Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.AUTH_ERROR);
        }
        int result = userMapper.deleteById(id);
        return result > 0;

    }

    /**
     * 用户退出登录
     *
     * @param request 请求
     * @return 是否退出成功
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATUS);
        return true;
    }

    /**
     * 判断用户是否为管理员
     *
     * @param request 请求
     * @return 是否为管理员
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User user = (User) userObj;
        return Objects.equals(user.getUserRole(), USER_ADMIN);
    }

    @Override
    public boolean isAdmin(User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        return loginUser.getUserRole().equals(USER_ADMIN);
    }

    /**
     * 脱敏用户信息
     *
     * @param user
     * @return
     */
    public User getSafetyUser(User user) {
        //脱敏用户信息
        User safelyUser = new User();
        safelyUser.setId(user.getId());
        safelyUser.setUserName(user.getUserName());
        safelyUser.setUserAccount(user.getUserAccount());
        safelyUser.setAge(user.getAge());
        safelyUser.setSex(user.getSex());
        safelyUser.setPhone(user.getPhone());
        safelyUser.setUserProfile(user.getUserProfile());
        safelyUser.setEmail(user.getEmail());
        safelyUser.setUserAvatar(user.getUserAvatar());
        safelyUser.setUserRole(user.getUserRole());
        safelyUser.setCreateTime(user.getCreateTime());
        safelyUser.setUserRole(user.getUserRole());
        safelyUser.setTags(user.getTags());

        return safelyUser;
    }

    /**
     * 获取当前用户信息
     *
     * @param request
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.AUTH_ERROR);
        }
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATUS);
        return (User) attribute;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATUS);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.AUTH_ERROR);
        }
        return (User) userObj;
    }

    @Override
    public int updateUser(User user, User loginUser) {

        Long userId = user.getId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 判断user除id外其余属性是否都为空
        User validUser = new User();
        BeanUtils.copyProperties(user, validUser);
        validUser.setId(null);
        if (ObjectUtils.allNull(validUser)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        if (!isAdmin(loginUser) && loginUser.getId() != userId) {
            throw new BusinessException(ErrorCode.AUTH_ERROR);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return userMapper.updateById(user);
    }

    @Override
    public List<User> searchUserByTagsName(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 第一种方式使用SQL查询
/*
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
*/

        // 第二种方式在内存查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        List<User> result = new ArrayList<>();
        Gson gson = new Gson();
        // 遍历用户列表，校验是否匹配tagNameList
        for (User user : userList) {
            boolean flag = true;
            String userTags = user.getTags();
            Set<String> tagNameSet = gson.fromJson(userTags, new TypeToken<Set<String>>() {
            }.getType());
            tagNameSet = Optional.ofNullable(tagNameSet).orElse(new HashSet<>());
            for (String temp : tagNameList) {
                if (!tagNameSet.contains(temp)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                result.add(user);
            }
        }
        return result.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }
}




