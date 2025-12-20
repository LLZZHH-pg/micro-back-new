package com.llzzhh.study.interactionservice.service;

import com.LLZZHH.study.dto.CommentDTO;

import java.util.List;

public interface CommentService {
    // 添加分页参数
    List<CommentDTO> listCommentsByContentId(String contentId, int page, int size);
    void commentContent(String contentId, String commentText);

    // 获取评论总数（用于分页）
    int countCommentsByContentId(String contentId);
}
