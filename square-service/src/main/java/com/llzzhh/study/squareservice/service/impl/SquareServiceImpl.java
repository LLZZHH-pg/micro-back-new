package com.llzzhh.study.squareservice.service.impl;

import com.LLZZHH.study.dto.ContentDTO;
import com.LLZZHH.study.dto.JwtUserDTO;
import com.LLZZHH.study.vo.ResultVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.llzzhh.study.squareservice.feign.ContentFeign;
import com.llzzhh.study.squareservice.service.SquareService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SquareServiceImpl implements SquareService {

    private final ContentFeign contentFeign;
    private final ObjectMapper objectMapper;

    @Override
    public List<ContentDTO> getContentsOrderedSquare(int page, int size) {
        try {
            // 通过Feign调用内容服务的广场内容接口
            ResultVO<List<ContentDTO>> result = contentFeign.getContentsSquare(page, size, getCurrentUserId());

            if (result != null && result.getCode() == 200) {
                return result.getData();
            } else {
                throw new RuntimeException("获取广场内容失败: " + (result != null ? result.getMsg() : "未知错误"));
            }
        } catch (Exception e) {
            throw new RuntimeException("调用内容服务失败: " + e.getMessage(), e);
        }
    }

    private Integer getCurrentUserId() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new SecurityException("无法获取请求上下文");
        }
        HttpServletRequest request = attrs.getRequest();
        String userJson = request.getHeader("X-User-Info");
        if (!StringUtils.hasText(userJson)) {
            throw new SecurityException("用户未认证，请登录后再试");
        }
        try {
            JwtUserDTO jwtUser = objectMapper.readValue(userJson, JwtUserDTO.class);
            if (jwtUser.getUid() == null) {
                throw new SecurityException("Token 中缺少用户ID");
            }
            return jwtUser.getUid();
        } catch (Exception e) {
            throw new SecurityException("解析用户信息失败", e);
        }
    }

}
