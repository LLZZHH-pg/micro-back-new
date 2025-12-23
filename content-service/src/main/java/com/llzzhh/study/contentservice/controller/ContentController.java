package com.llzzhh.study.contentservice.controller;

import com.llzzhh.study.contentservice.service.ContentService;
import com.LLZZHH.study.dto.ContentDTO;
import com.LLZZHH.study.vo.ResultVO;
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
        try {
            contentService.saveContent(contentDTO);
            return ResultVO.ok(null);
        } catch (SecurityException e) {
            return ResultVO.forbidden(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResultVO.fail(e.getMessage());
        } catch (Exception e) {
            return ResultVO.serverError(e.getMessage());
        }
    }

    @PostMapping("/contents/state")
    public ResultVO<Void> updateState(@RequestBody ContentDTO contentDTO) {
        try {
            contentService.updateContentState(contentDTO.getContentId(), contentDTO.getState());
            return ResultVO.ok(null);
        } catch (SecurityException e) {
            return ResultVO.forbidden(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResultVO.fail(e.getMessage());
        } catch (Exception e) {
            return ResultVO.serverError(e.getMessage());
        }
    }

    // 删除内容
    @PostMapping("/contents/delete")
    public ResultVO<Void> deleteContent(@RequestBody ContentDTO contentDTO) {
        try {
            contentService.deleteContent(contentDTO.getContentId());
            return ResultVO.ok(null);
        } catch (SecurityException e) {
            return ResultVO.forbidden(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResultVO.fail(e.getMessage());
        } catch (Exception e) {
            return ResultVO.serverError(e.getMessage());
        }
    }

    @PostMapping("/contents/updateLikes")
    public ResultVO<Void> updateLikes(@RequestParam String contentId, @RequestParam int increment) {
        contentService.updateLikes(contentId, increment);
        return ResultVO.ok(null);
    }

    // 文件上传
    @PostMapping("/upload")
    public ResultVO<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResultVO.ok(contentService.uploadFile(file));
    }

    @GetMapping("/contentsSquare")
    public ResultVO<List<ContentDTO>> getContentsSquare(@RequestParam (defaultValue = "1") int page , @RequestParam (defaultValue = "10") int pageSize,@RequestParam int userId) {
        return ResultVO.ok(contentService.getSquareContents(page, pageSize, userId));
    }

}
