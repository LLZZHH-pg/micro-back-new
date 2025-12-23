package com.llzzhh.study.squareservice.feign;

import com.LLZZHH.study.dto.ContentDTO;
import com.LLZZHH.study.vo.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "content-service")
public interface ContentFeign {

    @GetMapping("/api/user/contentsSquare")
    ResultVO<List<ContentDTO>> getContentsSquare(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam("userId") int userId
    );
}
