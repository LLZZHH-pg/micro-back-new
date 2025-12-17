package com.llzzhh.study.contentservice.controller;

import com.llzzhh.study.contentservice.service.ContentService;
import com.llzzhh.study.dto.ContentDTO;
import com.llzzhh.study.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    // 获取用户内容
    @GetMapping("/contents")
    public ResultVO<List<ContentDTO>> getContents(@RequestParam (defaultValue = "1") int page , @RequestParam (defaultValue = "10") int pageSize) {
        return ResultVO.ok(contentService.getContentsOrdered(page, pageSize));
    }

    // 保存内容
    @PostMapping("/contents")
    public ResultVO<Void> saveContent(@RequestBody ContentDTO contentDTO) {
        contentService.saveContent(contentDTO);
        return ResultVO.ok(null);
    }

    @PostMapping("/contents/state")
    public ResultVO<Void> updateState(@RequestBody ContentDTO contentDTO) {
        contentService.updateContentState(contentDTO.getContentId(), contentDTO.getState());
        return ResultVO.ok(null);
    }

    // 删除内容
    @PostMapping("/contents/delete")
    public ResultVO<Void> deleteContent(@RequestBody ContentDTO contentDTO) {
        contentService.deleteContent(contentDTO.getContentId());
        return ResultVO.ok(null);
    }


    // 文件上传
    @PostMapping("/upload")
    public ResultVO<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResultVO.ok(contentService.uploadFile(file));
    }

    @PostMapping("/contents/like")
    public ResultVO<Void> likeContent(@RequestBody ContentDTO contentDTO) {
        contentService.likeContent(contentDTO.getContentId());
        return ResultVO.ok(null);
    }
    @PostMapping("/contents/comment")
    public ResultVO<Void> commentContent(@RequestBody ContentDTO contentDTO) {
        contentService.commentContent(contentDTO.getContentId(), contentDTO.getCommentText());
        return ResultVO.ok(null);
    }


}
