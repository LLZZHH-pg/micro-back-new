package com.llzzhh.study.interactionservice.controller;

import com.LLZZHH.study.vo.ResultVO;
import com.llzzhh.study.interactionservice.entity.Comment;
import com.llzzhh.study.interactionservice.service.AdminCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.LLZZHH.study.dto.CommentDTO;
import lombok.extern.slf4j.Slf4j;

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
@RequestMapping("/api/admin/comment")
@RequiredArgsConstructor
@Slf4j

public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    @Value("${jwt.secret}")
    private String jwtSecret;


    // 原有的删除评论接口（保留）
    @DeleteMapping("/{commentId}")
    public ResultVO<String> deleteComment(HttpServletRequest request, @PathVariable String commentId) {
        if (!isAdmin(request)) {
            return ResultVO.fail("无权限：仅admin角色可执行此操作");
        }
        adminCommentService.deleteCommentById(commentId);
        return ResultVO.ok("评论删除成功");
    }

    // 修改评论内容的接口
    @PutMapping("/content")
    public ResultVO<String> updateCommentContent(HttpServletRequest request, @RequestBody CommentDTO dto) {
        try {
            if (!isAdmin(request)) {
                return ResultVO.fail("无权限：仅admin角色可执行此操作");
            }
            // 1. 从DTO中获取参数（和请求体key一致）
            String commentId = dto.getCommentId();
            String newContent = dto.getNewContent(); // 对应请求体的newContent

            // 2. 严格参数校验（解决空值导致的异常）
            if (commentId == null || commentId.isBlank()) {
                log.warn("评论ID为空");
                return ResultVO.fail("评论ID不能为空");
            }
            if (newContent == null || newContent.isBlank()) {
                log.warn("新评论内容为空");
                return ResultVO.fail("新评论内容不能为空");
            }

            // 3. 关键：检查评论ID是否真的存在于数据库（解决Updates=0）
            boolean exists = adminCommentService.checkCommentExists(commentId);
            if (!exists) {
                log.warn("数据库中不存在该评论ID：{}", commentId);
                return ResultVO.fail("评论ID不存在，无法修改");
            }

            // 4. 执行修改逻辑
            boolean success = adminCommentService.updateCommentContent(commentId, newContent);
            if (success) {
                log.info("评论修改成功：commentId={}, newContent={}", commentId, newContent);
                return ResultVO.ok("评论内容修改成功");
            } else {
                log.warn("评论修改失败（SQL未命中数据）：commentId={}", commentId);
                return ResultVO.fail("评论修改失败");
            }
        } catch (Exception e) {
            log.error("修改评论异常", e);
            return ResultVO.serverError("修改评论内容异常：" + e.getMessage());
        }
    }

    // 查询所有评论
    @GetMapping("/list") // 必须是@GetMapping，路径是/list
    public ResultVO<List<Comment>> getAllComments(HttpServletRequest request) {
        try {
            if (!isAdmin(request)) {
                return ResultVO.fail("无权限：仅admin角色可执行此操作");
            }
            List<Comment> comments = adminCommentService.getAllComments();
            return ResultVO.ok(comments);
        } catch (Exception e) {
            return ResultVO.serverError("查询用户评论失败：" + e.getMessage());
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
                // 验签失败时回退为只解码 payload（开发环境可用）
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