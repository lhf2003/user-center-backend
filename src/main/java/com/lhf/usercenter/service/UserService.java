package com.lhf.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lhf.usercenter.model.User;

import javax.servlet.http.HttpServletRequest;

/**
 * @author LHF
 * @description 针对表【user】的数据库操作Service
 * @createDate 2024-07-31 14:54:54
 */
public interface UserService extends IService<User> {
    boolean userRegister(String userAccount, String userPassword, String checkPassword);

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User searchUserByUserName(String userAccount, HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    boolean deleteUser(Long id, HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);
}
