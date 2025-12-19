package com.llzzhh.study.squareservice.service.impl;

import com.llzzhh.study.squareservice.feign.ContentFeign;
import com.llzzhh.study.squareservice.service.SquareService;
import com.llzzhh.study.dto.ContentDTO;
import com.llzzhh.study.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SquareServiceImpl implements SquareService {

    private final ContentFeign contentFeign;

    @Override
    public List<ContentDTO> getContentsOrderedSquare(int page, int size) {
        try {
            // 通过Feign调用内容服务的广场内容接口
            ResultVO<List<ContentDTO>> result = contentFeign.getContentsSquare(page, size);

            if (result != null && result.getCode() == 200) {
                return result.getData();
            } else {
                throw new RuntimeException("获取广场内容失败: " + (result != null ? result.getMsg() : "未知错误"));
            }
        } catch (Exception e) {
            throw new RuntimeException("调用内容服务失败: " + e.getMessage(), e);
        }
    }

}
