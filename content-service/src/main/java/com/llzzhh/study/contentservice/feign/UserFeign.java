package com.llzzhh.study.contentservice.feign;

import com.LLZZHH.study.vo.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service")
public interface UserFeign {

    @GetMapping("/api/user/profile/byId")
    ResultVO<Map<String, Object>> getUserProfileById(@RequestParam("userId") Integer userId);

    @GetMapping("/api/user/batch")
    ResultVO<Map<Integer, Map<String, Object>>> getUsersBatch(@RequestParam("userIds") List<Integer> userIds);
}
