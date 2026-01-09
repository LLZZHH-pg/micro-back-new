package com.llzzhh.study.interactionservice.service;

import com.llzzhh.study.interactionservice.entity.Comment;
import java.util.List;

public interface AdminCommentService {
    // 查询所有用户评论
    List<Comment> getAllComments();

    // 修改违规评论内容
    boolean updateCommentContent(String commentId, String newContent);

    // 删除违规评论
    boolean deleteCommentById(String commentId);

    boolean checkCommentExists(String commentId);
    // 关联查询：评论+对应的内容信息（便于管理员监控）
    List<Comment> getCommentsWithContent();
}