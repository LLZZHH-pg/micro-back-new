package com.llzzhh.study.interactionservice.service;

import com.llzzhh.study.dto.CommentDTO;

import java.util.List;

public interface CommentService {
    List<CommentDTO> listCommentsByContentId(String contentId);
    void commentContent(String contentId, String commentText);
}
