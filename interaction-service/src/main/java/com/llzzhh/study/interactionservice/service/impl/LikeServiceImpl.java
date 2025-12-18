package com.llzzhh.study.interactionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.llzzhh.study.interactionservice.entity.Like;
import com.llzzhh.study.interactionservice.service.LikeService;

import java.time.LocalDateTime;

public class LikeServiceImpl implements LikeService {
    @Override
    public void likeContent(String id) {
        if (id == null || id.isBlank() || "undefined".equals(id) || "null".equals(id)) {
            throw new IllegalArgumentException("无效的内容ID");
        }
        try {
            Like like = new Like();
            boolean isLike=isLike(id);
            if (!isLike) {
                like.setLikeId("like"+id + "_" + getCurrentUserId());
                like.setContentId(id);
                like.setUserId(getCurrentUserId());
                like.setCreateTime(LocalDateTime.now());
                likeMapper.insert(like);

                UpdateWrapper<Content> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", id)
                        .setSql("likes = likes + 1");
                contentMapper.update(null, updateWrapper);
            } else {
                String likeId = "like"+id + "_" + getCurrentUserId();
                likeMapper.deleteById(likeId);
                UpdateWrapper<Content> likeWrapper = new UpdateWrapper<>();
                likeWrapper.eq("id", id)
                        .setSql("likes = likes - 1");
                contentMapper.update(null, likeWrapper);
            }
        } catch (Exception e) {
            throw new RuntimeException("点赞内容失败: " + e.getMessage(), e);
        }
    }
    private boolean isLike(String id){
        return likeMapper.exists(new QueryWrapper<Like>()
                .eq("likeID", "like"+id + "_" + getCurrentUserId()));
    }
}
