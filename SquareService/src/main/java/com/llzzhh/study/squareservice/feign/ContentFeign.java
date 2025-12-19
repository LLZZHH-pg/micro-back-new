package com.llzzhh.study.squareservice.feign;

import com.llzzhh.study.dto.ContentDTO;
import com.llzzhh.study.vo.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "content-service")
public interface ContentFeign {

    @GetMapping("/api/user/contentsSquare")
    ResultVO<List<ContentDTO>> getContentsSquare(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );
}
