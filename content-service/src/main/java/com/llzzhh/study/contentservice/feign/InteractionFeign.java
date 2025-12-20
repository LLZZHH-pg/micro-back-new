package com.llzzhh.study.contentservice.feign;

import com.LLZZHH.study.vo.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "interaction-service")
public interface InteractionFeign {

    // 修改为支持分页
    @GetMapping("/api/user/interaction/comments")
    ResultVO<Map<String, Object>> getCommentsByContentId(@RequestParam("contentId") String contentId,
                                                         @RequestParam(value = "page", defaultValue = "1") int page,
                                                         @RequestParam(value = "size", defaultValue = "10") int size);

    @GetMapping("/api/user/interaction/isLiked")
    ResultVO<Boolean> checkIsLiked(@RequestParam("contentId") String contentId,
                                   @RequestParam("userId") Integer userId);
}
