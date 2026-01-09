package com.llzzhh.study.userservice.controller;

import com.LLZZHH.study.vo.ResultVO;
import com.llzzhh.study.userservice.entity.User;
import com.llzzhh.study.userservice.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员用户管理控制器（仅负责用户相关接口，删除所有评论/点赞接口）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/user") // 新增统一前缀，区分管理员接口和普通接口
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 查询所有用户
     */
    @GetMapping("/list")
    public ResultVO<List<User>> getAllUsers() {
        try {
            List<User> userList = adminUserService.getAllUsers();
            return ResultVO.ok(userList);
        } catch (Exception e) {
            return ResultVO.serverError("查询所有用户失败：" + e.getMessage());
        }
    }

    /**
     * 修改用户状态
     * @param paramMap 包含userId（Integer）和newState（String）
     */

    @PutMapping("/state")
    public ResultVO<String> updateUserState(@RequestBody Map<String, Object> paramMap) {
        try {
            if (paramMap == null || !paramMap.containsKey("userId") || !paramMap.containsKey("newState")) {
                return ResultVO.fail("参数错误：缺少userId或newState");
            }

            // 3. 解析并校验userId（兼容补零字符串/数字类型）
            Object userIdObj = paramMap.get("userId");
            Integer userId = parseZerofillUserId(userIdObj);
            if (userId == null || userId <= 0) {
                return ResultVO.fail("用户ID无效：必须是正整数（支持补零字符串如00123）");
            }

            // 4. 解析并校验newState
            Object newStateObj = paramMap.get("newState");
            if (!(newStateObj instanceof String)) {
                return ResultVO.fail("状态值错误：必须是字符串类型（仅支持0/1/2）");
            }
            String newState = ((String) newStateObj).trim();
            if (!"normal".equals(newState) && !"ban".equals(newState) && !"cancel".equals(newState)) {
                return ResultVO.fail("状态值不合法：仅支持normal（正常）、ban（禁用）、cancel（删除）");
            }
            boolean success = adminUserService.updateUserState(userId,newState);
            if (success) {
                // 改为调用自定义提示语的success方法（和你的ok风格对齐）
                return ResultVO.ok("用户状态更新成功");
            } else {
                // 改为调用自定义提示语的fail方法
                return ResultVO.fail("状态更新失败：用户不存在或状态未变更");
            }
        } catch (Exception e) {
            // 改为调用自定义提示语的serverError方法
            return ResultVO.serverError("更新用户状态异常：" + e.getMessage());
        }
    }


    @GetMapping("/byRole")
    public ResultVO<List<User>> getUsersByRole(@RequestParam String role) {
        try {
            // 新增：参数校验
            if (!StringUtils.hasText(role)) {
                return ResultVO.fail("角色参数不能为空");
            }

            List<User> userList = adminUserService.getUsersByRole(role.trim());
            return ResultVO.ok(userList);
        } catch (Exception e) {
            return ResultVO.serverError("按角色查询用户失败：" + e.getMessage());
        }
    }
    @PutMapping("/role")
    public ResultVO<String> updateUserRole(@RequestBody Map<String, Object> paramMap) {
        Integer userId = parseZerofillUserId(paramMap.get("userId"));
        String newRole = (String) paramMap.get("newRole");

        if (userId == null || !StringUtils.hasText(newRole)) {
            return ResultVO.fail("用户ID和新角色不能为空");
        }
        if (!"user".equals(newRole) && !"admin".equals(newRole)) {
            return ResultVO.fail("新角色仅允许为user或admin");
        }

        boolean success = adminUserService.updateUserRole(userId, newRole);
        if (success) {
            return ResultVO.ok("用户ROL角色修改成功");
        } else {
            return ResultVO.fail("角色修改失败（用户不存在或参数错误）");
        }
    }

    // 工具方法：处理补零字符串→数字（核心功能保留）
    private Integer parseZerofillUserId(Object userIdObj) {
        if (userIdObj == null) {
            return null;
        }
        try {
            String userIdStr = userIdObj.toString().replaceAll("^0+", "");
            if (userIdStr.isEmpty()) {
                userIdStr = "0";
            }
            return Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            // 去掉日志打印，直接返回null
            return null;
        }
    }
}