package com.llzzhh.study.interactionservice.service;

public interface LikeService {
    void likeContent(String contentId);
    boolean isLiked(String contentId, Integer userId);
}
