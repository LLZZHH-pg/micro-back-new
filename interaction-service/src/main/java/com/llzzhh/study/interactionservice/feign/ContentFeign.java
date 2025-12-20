package com.llzzhh.study.interactionservice.feign;

import com.LLZZHH.study.vo.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "content-service")
public interface ContentFeign {

    @PostMapping("/api/user/content/updateLikes")
    ResultVO<Void> updateLikes(@RequestParam("contentId") String contentId,
                               @RequestParam("increment") int increment);
}