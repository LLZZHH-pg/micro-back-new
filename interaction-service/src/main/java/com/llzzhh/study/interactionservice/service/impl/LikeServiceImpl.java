package com.llzzhh.study.interactionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.llzzhh.study.interactionservice.feign.ContentFeign;
import com.llzzhh.study.interactionservice.entity.Like;
import com.llzzhh.study.interactionservice.mapper.LikeMapper;
import com.llzzhh.study.interactionservice.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;
    private final ContentFeign contentFeign; // 注入Feign客户端

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.llzzhh.study.dto.JwtUserDTO) {
                return ((com.llzzhh.study.dto.JwtUserDTO) principal).getUid();
            } else {
                throw new SecurityException("用户信息格式不正确");
            }
        }
        throw new SecurityException("用户未认证");
    }
}
