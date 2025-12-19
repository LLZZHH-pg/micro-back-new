package com.llzzhh.study.interactionservice.controller;

import com.llzzhh.study.dto.CommentDTO;
import com.llzzhh.study.dto.ContentDTO;
import com.llzzhh.study.interactionservice.service.CommentService;
import com.llzzhh.study.interactionservice.service.LikeService;
import com.llzzhh.study.vo.ResultVO;
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
    @PostMapping("/comment")
    public ResultVO<Void> commentContent(@RequestBody ContentDTO contentDTO) {
        commentService.commentContent(contentDTO.getContentId(), contentDTO.getCommentText());
        return ResultVO.ok(null);
    }

    @GetMapping("/comments")
    public ResultVO<Map<String, Object>> getCommentsByContentId(
            @RequestParam String contentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 获取分页评论
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
