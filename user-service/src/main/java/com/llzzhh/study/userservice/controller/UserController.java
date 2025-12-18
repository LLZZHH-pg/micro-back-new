package com.llzzhh.study.userservice.controller;
import com.llzzhh.study.dto.JwtUserDTO;
import com.llzzhh.study.vo.ResultVO;
import com.llzzhh.study.dto.LoginDTO;
import com.llzzhh.study.dto.RegisterDTO;
import com.llzzhh.study.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

//    @PutMapping("/profile")
//    public ResultVO<String> updateProfile(@RequestBody User updateUser) {
//        try {
//            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//            if (auth == null || auth.getPrincipal() == null) {
//                return ResultVO.unauthorized("用户未登录");
//            }
//            User currentUser = (User) auth.getPrincipal();
//            updateUser.setUid(currentUser.getUid());
//            userService.updateProfile(updateUser);
//            return ResultVO.ok("更新成功");
//        } catch (Exception e) {
//            return ResultVO.fail(e.getMessage());
//        }
//    }

//    @PostMapping("/logout")
//    public ResultVO<String> logout() {
//        try {
//            SecurityContextHolder.clearContext();
//            return ResultVO.ok("退出登录成功");
//        } catch (Exception e) {
//            return ResultVO.serverError("退出登录失败");
//        }
//    }
}
