package com.llzzhh.study.interactionservice.controller;

import com.LLZZHH.study.vo.ResultVO;
import com.llzzhh.study.interactionservice.entity.Like;
import com.llzzhh.study.interactionservice.service.AdminLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/like")
@RequiredArgsConstructor
public class AdminLikeController {

    private final AdminLikeService adminLikeService;

    // 查询所有点赞记录（保留）
    @GetMapping("/list")
    public ResultVO<List<Like>> getAllLikes() {
        try {
            List<Like> likes = adminLikeService.getAllLikes();
            return ResultVO.ok(likes);
        } catch (Exception e) {
            return ResultVO.serverError("查询点赞记录失败：" + e.getMessage());
        }
    }

    // 按内容ID查点赞（保留）
    @GetMapping("/by-content-id")
    public ResultVO<List<Like>> getLikesByContentId(@RequestParam String contentId) {
        try {
            List<Like> likes = adminLikeService.getLikesByContentId(contentId);
            return ResultVO.ok(likes);
        } catch (Exception e) {
            return ResultVO.serverError("查询内容点赞记录失败：" + e.getMessage());
        }
    }

    // 关联查询：点赞+内容（保留）
    @GetMapping("/list-with-content")
    public ResultVO<List<Like>> getLikesWithContent() {
        try {
            List<Like> likes = adminLikeService.getLikesWithContent();
            return ResultVO.ok(likes);
        } catch (Exception e) {
            return ResultVO.serverError("查询点赞及对应内容失败：" + e.getMessage());
        }
    }
    @DeleteMapping("/{likeId}") // 路径：/api/admin/like/xxx（匹配请求的Path参数）
    public ResultVO<String> deleteLike(@PathVariable("likeId") String likeId) {
        try {
            // 直接调用Service删除
            boolean deleteSuccess = adminLikeService.deleteLikeById(likeId);
            if (deleteSuccess) {
                return ResultVO.ok("删除点赞记录成功");
            } else {
                return ResultVO.fail("点赞记录不存在（likeId错误）");
            }
        } catch (Exception e) {
            return ResultVO.serverError("删除失败：" + e.getMessage());
        }
    }
}