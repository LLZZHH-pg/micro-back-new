package com.llzzhh.study.interactionservice.service.impl;

import com.llzzhh.study.dto.CommentDTO;
import com.llzzhh.study.interactionservice.entity.Comment;
import com.llzzhh.study.interactionservice.mapper.CommentMapper;
import com.llzzhh.study.interactionservice.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentMapper commentMapper;

    @Override
    public List<CommentDTO> listCommentsByContentId(String contentId) {
        List<Comment> comments = commentMapper.selectCommentsWithUsername(contentId);
        return comments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private CommentDTO toDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setCommentId(comment.getCommentId());
        dto.setContentId(comment.getContentId());
        dto.setUserId(comment.getUserId());
        dto.setCommentText(comment.getCommentText());
        dto.setCreateTime(comment.getCreateTime());
        dto.setUsername(comment.getUsername());
        return dto;
    }

    @Override
    public void commentContent(String id, String commentText) {
        if (id == null || id.isBlank() || "undefined".equals(id) || "null".equals(id)) {
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
            comment.setContentId(id);
            comment.setUserId(getCurrentUserId());
            comment.setCommentText(commentText);
            comment.setCreateTime(LocalDateTime.now());
            commentMapper.insert(comment);
        }catch (Exception e){
            throw new RuntimeException("评论内容失败: " + e.getMessage(), e);
        }

    }

}
