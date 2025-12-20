package com.llzzhh.study.interactionservice.feign;

import com.LLZZHH.study.vo.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "user-service")
public interface UserFeign {

    @GetMapping("/api/user/batch")
    ResultVO<Map<Integer, Map<String, Object>>> getUsersBatch(@RequestParam("userIds") String userIds);
}
