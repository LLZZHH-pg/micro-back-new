package com.llzzhh.study.interactionservice.service.impl;

import com.llzzhh.study.interactionservice.entity.Comment;
import com.llzzhh.study.interactionservice.mapper.AdminCommentMapper;
import com.llzzhh.study.interactionservice.service.AdminCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCommentServiceImpl implements AdminCommentService {

    // 注入已写好的AdminCommentMapper
    private final AdminCommentMapper adminCommentMapper;

    @Override
    public List<Comment> getAllComments() {
        // 调用Mapper的查询方法
        return adminCommentMapper.selectAllComments();
    }

    @Override
    public boolean updateCommentContent(String commentId, String newContent) {
        // 参数合法性校验（和现有业务逻辑风格一致，避免无效参数）
        if (commentId == null || commentId.trim().isEmpty() || newContent == null || newContent.trim().isEmpty()) {
            return false;
        }
        // 调用Mapper的更新方法，判断受影响行数是否大于0
        int affectedRows = adminCommentMapper.updateCommentContentById(commentId, newContent.trim());
        return affectedRows > 0;
    }

    @Override
    public boolean deleteCommentById(String commentId) {
        if (commentId == null || commentId.trim().isEmpty()) {
            return false;
        }
        // 调用Mapper的删除方法
        int affectedRows = adminCommentMapper.deleteCommentById(commentId);
        return affectedRows > 0;
    }

    @Override
    public List<Comment> getCommentsWithContent() {
        // 调用Mapper的关联查询方法
        return adminCommentMapper.selectCommentsWithContent();
    }
    @Override
    public boolean checkCommentExists(String commentId) {
        // 优先用MyBatis-Plus的selectById（如果Comment实体主键映射正确）
        Comment comment = adminCommentMapper.selectById(commentId);
        return comment != null;
    }
}