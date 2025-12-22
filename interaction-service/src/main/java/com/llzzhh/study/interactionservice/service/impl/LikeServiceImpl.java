package com.llzzhh.study.interactionservice.service.impl;

import com.LLZZHH.study.dto.JwtUserDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.llzzhh.study.interactionservice.feign.ContentFeign;
import com.llzzhh.study.interactionservice.entity.Like;
import com.llzzhh.study.interactionservice.mapper.LikeMapper;
import com.llzzhh.study.interactionservice.service.LikeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;
    private final ContentFeign contentFeign;
    private final ObjectMapper objectMapper;

    @Override
    public void likeContent(String contentId) {
        if (contentId == null || contentId.isBlank() || "undefined".equals(contentId) || "null".equals(contentId)) {
            throw new IllegalArgumentException("无效的内容ID");
        }

        Integer currentUserId = getCurrentUserId();
        boolean isLiked = isLiked(contentId, currentUserId);

        try {
            if (!isLiked) {
                // 点赞
                Like like = new Like();
                like.setLikeId("like" + contentId + "_" + currentUserId);
                like.setContentId(contentId);
                like.setUserId(currentUserId);
                like.setCreateTime(LocalDateTime.now());
                likeMapper.insert(like);

                // 调用content服务更新点赞数
                contentFeign.updateLikes(contentId, 1);
            } else {
                // 取消点赞
                String likeId = "like" + contentId + "_" + currentUserId;
                likeMapper.deleteById(likeId);

                // 调用content服务更新点赞数
                contentFeign.updateLikes(contentId, -1);
            }
        } catch (Exception e) {
            throw new RuntimeException("点赞操作失败: " + e.getMessage(), e);
        }
    }

    public boolean isLiked(String contentId, Integer userId) {
        return likeMapper.exists(new QueryWrapper<Like>()
                .eq("likeID", "like" + contentId + "_" + userId));
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
