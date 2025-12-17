package com.llzzhh.study.interactionservice.service.impl;

import com.llzzhh.study.dto.CommentDTO;
import com.llzzhh.study.interactionservice.entity.Comment;
import com.llzzhh.study.interactionservice.mapper.CommentMapper;
import com.llzzhh.study.interactionservice.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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

}
