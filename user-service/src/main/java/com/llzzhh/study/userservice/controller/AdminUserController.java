package com.llzzhh.study.userservice.controller;

import com.LLZZHH.study.vo.ResultVO;
import com.llzzhh.study.userservice.entity.User;
import com.llzzhh.study.userservice.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * 管理员用户管理控制器（仅负责用户相关接口，删除所有评论/点赞接口）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/user") // 新增统一前缀，区分管理员接口和普通接口
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * 查询所有用户
     */
    @GetMapping("/list")
    public ResultVO<List<User>> getAllUsers(HttpServletRequest request) {
        try {
            if (!isAdmin(request)) {
                return ResultVO.fail("无权限：仅admin角色可执行此操作");
            }
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
    public ResultVO<String> updateUserState(HttpServletRequest request, @RequestBody Map<String, Object> paramMap) {
        try {
            if (!isAdmin(request)) {
                return ResultVO.fail("无权限：仅admin角色可执行此操作");
            }
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
    public ResultVO<List<User>> getUsersByRole(HttpServletRequest request, @RequestParam String role) {
        try {
            if (!isAdmin(request)) {
                return ResultVO.fail("无权限：仅admin角色可执行此操作");
            }
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
    public ResultVO<String> updateUserRole(HttpServletRequest request, @RequestBody Map<String, Object> paramMap) {
        if (!isAdmin(request)) {
            return ResultVO.fail("无权限：仅admin角色可执行此操作");
        }
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

    // 校验请求中 JWT 是否为 admin 角色（简易实现：解析 Authorization: Bearer <token>）
    private boolean isAdmin(HttpServletRequest request) {
        try {
            String auth = request.getHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                return false;
            }
            String token = auth.substring(7);
            try {
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
                Object userObj = claims.get("user");
                if (userObj instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) userObj;
                    Object role = m.get("role");
                    return "admin".equals(role);
                }
                return false;
            } catch (Exception ex) {
                String role = decodeRoleFromTokenWithoutVerification(token);
                return "admin".equals(role);
            }
        } catch (Exception e) {
            return false;
        }
    }

    private String decodeRoleFromTokenWithoutVerification(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payload = parts[1];
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            String json = new String(decoded, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            Map<?, ?> map = mapper.readValue(json, Map.class);
            Object userObj = map.get("user");
            if (userObj instanceof Map) {
                Object role = ((Map<?, ?>) userObj).get("role");
                return role == null ? null : role.toString();
            }
            Object role = map.get("role");
            return role == null ? null : role.toString();
        } catch (Exception e) {
            return null;
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