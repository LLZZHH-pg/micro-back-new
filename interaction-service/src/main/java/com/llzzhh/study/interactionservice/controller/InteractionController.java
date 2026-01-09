package com.llzzhh.study.interactionservice.controller;

import com.LLZZHH.study.dto.CommentDTO;
import com.LLZZHH.study.dto.ContentDTO;
import com.llzzhh.study.interactionservice.service.CommentService;
import com.llzzhh.study.interactionservice.service.LikeService;
import com.LLZZHH.study.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/interaction")
@RequiredArgsConstructor
public class InteractionController {
    private final LikeService likeService;
    private final CommentService commentService;

    @PostMapping("/like")
    public ResultVO<Void> likeContent(@RequestBody ContentDTO contentDTO) {
        likeService.likeContent(contentDTO.getContentId());
        return ResultVO.ok(null);
    }

    // 核心修复：只传2个参数，匹配Service接口定义（userId自动从请求头获取）
    @PostMapping("/comment")
    public ResultVO<Void> commentContent(@RequestBody CommentDTO commentDTO) {
        // 只传contentId和commentText，userId由Service自动获取，无需手动传！
        commentService.commentContent(commentDTO.getContentId(), commentDTO.getCommentText());
        return ResultVO.ok(null);
    }

    @GetMapping("/comments")
    public ResultVO<Map<String, Object>> getCommentsByContentId(
            @RequestParam String contentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 获取分页评论（匹配Service的参数：contentId, page, size）
        List<CommentDTO> comments = commentService.listCommentsByContentId(contentId, page, size);

        // 获取评论总数
        int total = commentService.countCommentsByContentId(contentId);

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / size);

        // 返回分页数据
        Map<String, Object> result = new HashMap<>();
        result.put("comments", comments);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("total", total);
        result.put("totalPages", totalPages);

        return ResultVO.ok(result);
    }

    @GetMapping("/isLiked")
    public ResultVO<Boolean> checkIsLiked(@RequestParam String contentId, @RequestParam Integer userId) {
        boolean isLiked = likeService.isLiked(contentId, userId);
        return ResultVO.ok(isLiked);
    }
}