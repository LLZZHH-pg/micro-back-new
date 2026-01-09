package com.llzzhh.study.interactionservice.controller;

import com.LLZZHH.study.vo.ResultVO;
import com.llzzhh.study.interactionservice.entity.Comment;
import com.llzzhh.study.interactionservice.service.AdminCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.LLZZHH.study.dto.CommentDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/admin/comment")
@RequiredArgsConstructor
@Slf4j

public class AdminCommentController {

    private final AdminCommentService adminCommentService;


    // 原有的删除评论接口（保留）
    @DeleteMapping("/{commentId}")
    public ResultVO<String> deleteComment(@PathVariable String commentId) {
        adminCommentService.deleteCommentById(commentId);
        return ResultVO.ok("评论删除成功");
    }

    // 修改评论内容的接口
    @PutMapping("/content")
    public ResultVO<String> updateCommentContent(@RequestBody CommentDTO dto) {
        try {
            // 1. 从DTO中获取参数（和请求体key一致）
            String commentId = dto.getCommentId();
            String newContent = dto.getNewContent(); // 对应请求体的newContent

            // 2. 严格参数校验（解决空值导致的异常）
            if (commentId == null || commentId.isBlank()) {
                log.warn("评论ID为空");
                return ResultVO.fail("评论ID不能为空");
            }
            if (newContent == null || newContent.isBlank()) {
                log.warn("新评论内容为空");
                return ResultVO.fail("新评论内容不能为空");
            }

            // 3. 关键：检查评论ID是否真的存在于数据库（解决Updates=0）
            boolean exists = adminCommentService.checkCommentExists(commentId);
            if (!exists) {
                log.warn("数据库中不存在该评论ID：{}", commentId);
                return ResultVO.fail("评论ID不存在，无法修改");
            }

            // 4. 执行修改逻辑
            boolean success = adminCommentService.updateCommentContent(commentId, newContent);
            if (success) {
                log.info("评论修改成功：commentId={}, newContent={}", commentId, newContent);
                return ResultVO.ok("评论内容修改成功");
            } else {
                log.warn("评论修改失败（SQL未命中数据）：commentId={}", commentId);
                return ResultVO.fail("评论修改失败");
            }
        } catch (Exception e) {
            log.error("修改评论异常", e);
            return ResultVO.serverError("修改评论内容异常：" + e.getMessage());
        }
    }

    // 查询所有评论
    @GetMapping("/list") // 必须是@GetMapping，路径是/list
    public ResultVO<List<Comment>> getAllComments() {
        try {
            List<Comment> comments = adminCommentService.getAllComments();
            return ResultVO.ok(comments);
        } catch (Exception e) {
            return ResultVO.serverError("查询用户评论失败：" + e.getMessage());
        }
    }
}