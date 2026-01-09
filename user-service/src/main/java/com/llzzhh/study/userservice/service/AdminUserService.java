package com.llzzhh.study.userservice.service;

import com.llzzhh.study.userservice.entity.User;
import java.util.List;

/**
 * 管理员用户服务接口（仅负责用户管理，删除所有互动服务相关方法）
 */
public interface AdminUserService {

    List<User> getAllUsers();
    boolean updateUserState(Integer userId, String newState);
    List<User> getUsersByRole(String ROL);
    boolean updateUserRole(Integer userId, String newRole);
}