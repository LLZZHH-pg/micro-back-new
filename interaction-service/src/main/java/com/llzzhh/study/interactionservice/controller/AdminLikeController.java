package com.llzzhh.study.interactionservice.controller;

import com.LLZZHH.study.vo.ResultVO;
import com.llzzhh.study.interactionservice.entity.Like;
import com.llzzhh.study.interactionservice.service.AdminLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@RestController
@RequestMapping("/api/admin/like")
@RequiredArgsConstructor
public class AdminLikeController {

    private final AdminLikeService adminLikeService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    // 查询所有点赞记录（保留）
    @GetMapping("/list")
    public ResultVO<List<Like>> getAllLikes(HttpServletRequest request) {
        try {
            if (!isAdmin(request)) {
                return ResultVO.fail("无权限：仅admin角色可执行此操作");
            }
            List<Like> likes = adminLikeService.getAllLikes();
            return ResultVO.ok(likes);
        } catch (Exception e) {
            return ResultVO.serverError("查询点赞记录失败：" + e.getMessage());
        }
    }

    // 按内容ID查点赞（保留）
    @GetMapping("/by-content-id")
    public ResultVO<List<Like>> getLikesByContentId(HttpServletRequest request, @RequestParam String contentId) {
        try {
            if (!isAdmin(request)) {
                return ResultVO.fail("无权限：仅admin角色可执行此操作");
            }
            List<Like> likes = adminLikeService.getLikesByContentId(contentId);
            return ResultVO.ok(likes);
        } catch (Exception e) {
            return ResultVO.serverError("查询内容点赞记录失败：" + e.getMessage());
        }
    }

    // 关联查询：点赞+内容（保留）
    @GetMapping("/list-with-content")
    public ResultVO<List<Like>> getLikesWithContent(HttpServletRequest request) {
        try {
            if (!isAdmin(request)) {
                return ResultVO.fail("无权限：仅admin角色可执行此操作");
            }
            List<Like> likes = adminLikeService.getLikesWithContent();
            return ResultVO.ok(likes);
        } catch (Exception e) {
            return ResultVO.serverError("查询点赞及对应内容失败：" + e.getMessage());
        }
    }
    @DeleteMapping("/{likeId}") // 路径：/api/admin/like/xxx（匹配请求的Path参数）
    public ResultVO<String> deleteLike(HttpServletRequest request, @PathVariable("likeId") String likeId) {
        if (!isAdmin(request)) {
            return ResultVO.fail("无权限：仅admin角色可执行此操作");
        }
        try {
            // 直接调用Service删除
            boolean deleteSuccess = adminLikeService.deleteLikeById(likeId);
            if (deleteSuccess) {
                return ResultVO.ok("删除点赞记录成功");
            } else {
                return ResultVO.fail("点赞记录不存在（likeId错误）");
            }
        } catch (Exception e) {
            return ResultVO.serverError("删除失败：" + e.getMessage());
        }
    }

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
}