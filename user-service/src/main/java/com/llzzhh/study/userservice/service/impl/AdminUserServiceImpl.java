package com.llzzhh.study.userservice.service.impl;

import com.llzzhh.study.userservice.entity.User;
import com.llzzhh.study.userservice.mapper.AdminUserMapper;
import com.llzzhh.study.userservice.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 管理员用户服务实现类（仅负责用户管理，删除所有互动服务相关代码）
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class) // 明确事务回滚条件，更健壮\

public class AdminUserServiceImpl implements AdminUserService {

    // 仅保留用户管理相关Mapper，删除冗余的Feign客户端注入
    private final AdminUserMapper adminUserMapper;

    /**
     * 查询所有用户
     * @return 所有用户列表（无数据返回空列表）
     */
    @Override
    public List<User> getAllUsers() {
        List<User> userList = adminUserMapper.selectAllUsers();
        // 避免返回null，统一返回空列表更安全
        return userList == null ? Collections.emptyList() : userList;
    }

    /**
     * 修改用户状态
     * @param userId 用户ID
     * @param newState 新状态（如：0-正常、1-禁用、2-删除）
     * @return 操作是否成功
     */
    @Override
    public boolean updateUserState(Integer userId, String newState) {
        // 1. 兜底参数校验（防止控制器层校验遗漏）
        if (userId == null || userId <= 0) {
            return false;
        }
        if (newState == null) {
            return false;
        }
        String trimmedState = newState.trim();
        // 关键修改：替换状态值校验规则
        if (!"normal".equals(trimmedState) && !"ban".equals(trimmedState) && !"cancel".equals(trimmedState)) {
            return false;
        }
        // 3. 调用Mapper执行数据库更新
        int affectedRows = adminUserMapper.updateUserStateById(userId, trimmedState);
        // 受影响行数>0表示更新成功
        return affectedRows > 0;
    }
 /**
     * 按角色查询用户
     * @param role 角色标识（如：admin-管理员、user-普通用户）
     * @return 对应角色的用户列表
     */
    @Override
    public List<User> getUsersByRole(String role) {
        // 1. 参数校验（用Spring工具类更规范）
        if (!StringUtils.hasText(role)) {
            return Collections.emptyList();
        }

        // 2. 执行查询，避免返回null
        List<User> userList = adminUserMapper.selectUsersByRole(role.trim());
        return userList == null ? Collections.emptyList() : userList;
    }
    @Override
    public boolean updateUserRole(Integer userId, String newRole) {
        if (userId == null || !StringUtils.hasText(newRole)
                || (!"user".equals(newRole) && !"admin".equals(newRole))) {
            return false;
        }
        int affectedRows = adminUserMapper.updateUserRoleById(userId, newRole);
        return affectedRows > 0;
    }

}