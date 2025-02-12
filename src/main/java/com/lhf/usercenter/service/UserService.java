package com.lhf.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lhf.usercenter.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author LHF
 * @description 针对表【user】的数据库操作Service
 * @createDate 2024-07-31 14:54:54
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 是否成功
     */
    boolean userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request 请求
     * @return 用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 删除用户
     *
     * @param id 用户id
     * @param request 请求
     * @return 是否成功
     */
    boolean deleteUser(Long id, HttpServletRequest request);

    /**
     * 用户退出
     *
     * @param request 请求
     * @return 是否成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 根据用户名查询用户
     *
     * @param userAccount 用户账号
     * @param request 请求
     * @return 用户信息
     */
    User searchUserByUserName(String userAccount, HttpServletRequest request);

    /**
     * 根据标签名查询用户
     *
     * @param tagNameList 标签名列表
     * @return 用户信息
     */
    List<User> searchUserByTagsName(List<String> tagNameList);

    /**
     * 判断当前用户是否为管理员
     *
     * @param request 请求
     * @return 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 判断当前用户是否为管理员
     *
     * @param loginUser 登录用户
     * @return 是否为管理员
     */
    boolean isAdmin(User loginUser);
    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    User getSafetyUser(User user);

    /**
     * 获取当前用户信息
     *
     * @param request 请求
     * @return 当前用户信息
     */
    User getCurrentUser(HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    int updateUser(User user, User loginUser);
}
