package com.llzzhh.study.interactionservice.service;

import com.llzzhh.study.interactionservice.entity.Like;

import java.util.List;

public interface AdminLikeService {
    // 查询所有点赞记录（保留）
    List<Like> getAllLikes();

    boolean deleteLikeById(String likeId);

    // 按内容ID查点赞（保留）
    List<Like> getLikesByContentId(String contentId);

    // 关联查询点赞+内容（保留）
    List<Like> getLikesWithContent();
}