package com.llzzhh.study.userservice.controller;
import com.llzzhh.study.dto.JwtUserDTO;
import com.llzzhh.study.userservice.entity.User;
import com.llzzhh.study.vo.ResultVO;
import com.llzzhh.study.dto.LoginDTO;
import com.llzzhh.study.dto.RegisterDTO;
import com.llzzhh.study.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/auth/register")
    public ResultVO<String> register(@RequestBody RegisterDTO dto) {
        try {
            return ResultVO.ok(userService.register(dto));
        } catch (Exception e) {
            return ResultVO.fail(e.getMessage());
        }
    }

    @PostMapping("/auth/login")
    public ResultVO<String> login(@RequestBody LoginDTO dto) {
        try {
            return ResultVO.ok(userService.login(dto));
        } catch (Exception e) {
            return ResultVO.unauthorized(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResultVO<JwtUserDTO> getProfile() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null) {
                return ResultVO.unauthorized("用户未登录");
            }
            // 直接返回JwtUserDTO
            JwtUserDTO user = (JwtUserDTO) auth.getPrincipal();
            return ResultVO.ok(user);
        } catch (Exception e) {
            return ResultVO.serverError("获取用户信息失败");
        }
    }
    @GetMapping("/profile/byId")
    public ResultVO<Map<String, Object>> getUserProfileById(@RequestParam Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResultVO.fail("用户不存在");
            }

            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("uid", user.getUid());
            userProfile.put("name", user.getName());
            userProfile.put("sta", user.getState()); // 用户状态字段
            userProfile.put("email", user.getEmail());
            // 添加其他需要的字段...

            return ResultVO.ok(userProfile);
        } catch (Exception e) {
            return ResultVO.serverError("获取用户信息失败");
        }
    }

    @GetMapping("/batch")
    public ResultVO<Map<Integer, Map<String, Object>>> getUsersBatch(@RequestParam String userIds) {
        try {
            List<Integer> ids = Arrays.stream(userIds.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            List<User> users = userService.getUsersByIds(ids);
            Map<Integer, Map<String, Object>> result = new HashMap<>();

            for (User user : users) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("uid", user.getUid());
                userInfo.put("name", user.getName());
                userInfo.put("state", user.getState());
                result.put(user.getUid(), userInfo);
            }

            return ResultVO.ok(result);
        } catch (Exception e) {
            return ResultVO.serverError("批量获取用户信息失败");
        }
    }
}
