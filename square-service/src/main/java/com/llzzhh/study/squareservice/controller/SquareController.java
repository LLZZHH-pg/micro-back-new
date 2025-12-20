package com.llzzhh.study.squareservice.controller;


import com.LLZZHH.study.dto.ContentDTO;
import com.llzzhh.study.squareservice.service.SquareService;
import com.LLZZHH.study.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor

public class SquareController {
    private final SquareService contentService;

    @GetMapping("/contentsSquare")
    public ResultVO<List<ContentDTO>> getContentsSquare(@RequestParam(defaultValue = "1") int page , @RequestParam (defaultValue = "10") int pageSize) {
        return ResultVO.ok(contentService.getContentsOrderedSquare(page, pageSize));
    }
}