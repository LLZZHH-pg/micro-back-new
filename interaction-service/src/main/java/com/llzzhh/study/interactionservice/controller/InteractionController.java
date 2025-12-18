package com.llzzhh.study.interactionservice.controller;

import com.llzzhh.study.dto.ContentDTO;
import com.llzzhh.study.interactionservice.service.CommentService;
import com.llzzhh.study.interactionservice.service.LikeService;
import com.llzzhh.study.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class InteractionController {
    private final LikeService likeService;
    private final CommentService commentService;

    @PostMapping("/interaction/like")
    public ResultVO<Void> likeContent(@RequestBody ContentDTO contentDTO) {
        likeService.likeContent(contentDTO.getContentId());
        return ResultVO.ok(null);
    }
    @PostMapping("/interaction/comment")
    public ResultVO<Void> commentContent(@RequestBody ContentDTO contentDTO) {
        commentService.commentContent(contentDTO.getContentId(), contentDTO.getCommentText());
        return ResultVO.ok(null);
    }
}
