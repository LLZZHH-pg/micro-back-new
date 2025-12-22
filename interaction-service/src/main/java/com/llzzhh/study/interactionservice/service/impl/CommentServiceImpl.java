package com.llzzhh.study.interactionservice.service.impl;

import com.LLZZHH.study.dto.CommentDTO;
import com.LLZZHH.study.dto.JwtUserDTO;
import com.LLZZHH.study.vo.ResultVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.llzzhh.study.interactionservice.entity.Comment;
import com.llzzhh.study.interactionservice.feign.UserFeign;
import com.llzzhh.study.interactionservice.mapper.CommentMapper;
import com.llzzhh.study.interactionservice.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentMapper commentMapper;
    private final UserFeign userFeign;
    private final ObjectMapper objectMapper;

    @Override
    public List<CommentDTO> listCommentsByContentId(String contentId, int page, int size) {
        if (page < 1) page = 1;
        int offset = (page - 1) * size;

        // 分页查询评论
        List<Comment> comments = commentMapper.selectCommentsByContentId(contentId, offset, size);

        // 如果没有评论，直接返回空列表
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }

        // 收集用户ID
        List<Integer> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量获取用户信息
        Map<Integer, Map<String, Object>> usersMap = getUsersBatch(userIds);

        // 过滤状态为"正常"的用户评论，并设置用户名
        return comments.stream()
                .filter(comment -> {
                    Map<String, Object> userInfo = usersMap.get(comment.getUserId());
                    return userInfo != null && "normal".equals(userInfo.get("state"));
                })
                .map(comment -> {
                    CommentDTO dto = toDTO(comment);
                    Map<String, Object> userInfo = usersMap.get(comment.getUserId());
                    if (userInfo != null) {
                        dto.setUsername((String) userInfo.get("name"));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public int countCommentsByContentId(String contentId) {
        return commentMapper.countCommentsByContentId(contentId);
    }

    private CommentDTO toDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setCommentId(comment.getCommentId());
        dto.setContentId(comment.getContentId());
        dto.setUserId(comment.getUserId());
        dto.setCommentText(comment.getCommentText());
        dto.setCreateTime(comment.getCreateTime());
        return dto;
    }

    @Override
    public void commentContent(String contentId, String commentText) {
        if (contentId == null || contentId.isBlank() || "undefined".equals(contentId) || "null".equals(contentId)) {
            throw new IllegalArgumentException("无效的内容ID");
        }
        if (commentText == null || commentText.isBlank()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if(commentText.length()>520){
            throw new IllegalArgumentException("评论内容过长，不能超过520个字符");
        }

        try{
            Comment comment = new Comment();
            comment.setCommentId("comment" + UUID.randomUUID());
            comment.setContentId(contentId);
            comment.setUserId(getCurrentUserId());
            comment.setCommentText(commentText);
            comment.setCreateTime(LocalDateTime.now());
            commentMapper.insert(comment);
        } catch (Exception e){
            throw new RuntimeException("评论内容失败: " + e.getMessage(), e);
        }
    }

    private Map<Integer, Map<String, Object>> getUsersBatch(List<Integer> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            // 将用户ID列表转换为逗号分隔的字符串
            String idsStr = userIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            ResultVO<Map<Integer, Map<String, Object>>> result = userFeign.getUsersBatch(idsStr);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
        }

        return new HashMap<>();
    }

    private Integer getCurrentUserId() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new SecurityException("无法获取请求上下文");
        }
        HttpServletRequest request = attrs.getRequest();
        String userJson = request.getHeader("X-User-Info");
        if (!StringUtils.hasText(userJson)) {
            throw new SecurityException("用户未认证，请登录后再试");
        }
        try {
            JwtUserDTO jwtUser = objectMapper.readValue(userJson, JwtUserDTO.class);
            if (jwtUser.getUid() == null) {
                throw new SecurityException("Token 中缺少用户ID");
            }
            return jwtUser.getUid();
        } catch (Exception e) {
            throw new SecurityException("解析用户信息失败", e);
        }
    }
}