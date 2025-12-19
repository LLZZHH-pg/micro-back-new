package com.llzzhh.study.contentservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import com.llzzhh.study.contentservice.feign.InteractionFeign;
import com.llzzhh.study.contentservice.feign.UserFeign;
import com.llzzhh.study.contentservice.service.ContentService;
import com.llzzhh.study.dto.CommentDTO;
import com.llzzhh.study.dto.ContentDTO;
import com.llzzhh.study.dto.JwtUserDTO;
import com.llzzhh.study.contentservice.entity.Content;
import com.llzzhh.study.contentservice.mapper.ContentMapper;
import com.llzzhh.study.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentMapper contentMapper;
    private final InteractionFeign interactionFeign;
    private final UserFeign userFeign;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url-path}")
    private String urlPath;


    @Override
    public List<ContentDTO> getContentsOrdered(int page, int size) {
        int offset = (page - 1) * size;

        // 批量查询内容
        List<Content> contents = contentMapper.selectSquareContentsWithUsername(offset, size);

        // 收集所有用户ID
        List<Integer> userIds = contents.stream()
                .map(Content::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量获取用户信息
        Map<Integer, Map<String, Object>> usersMap = getUsersBatch(userIds);

        // 过滤并转换DTO
        return contents.stream()
                .filter(content -> {
                    // 检查用户状态是否为"normal"
                    Map<String, Object> userInfo = usersMap.get(content.getUserId());
                    if (userInfo == null) return false;
                    return "normal".equals(userInfo.get("sta"));
                })
                .map(content -> {
                    ContentDTO dto = convertToDTO(content);
                    // 设置用户名
                    Map<String, Object> userInfo = usersMap.get(content.getUserId());
                    if (userInfo != null) {
                        dto.setUsername((String) userInfo.get("name"));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private Map<Integer, Map<String, Object>> getUsersBatch(List<Integer> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            // 将用户ID列表转换为逗号分隔的字符串
            String idsStr = userIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            ResultVO<Map<Integer, Map<String, Object>>> result = userFeign.getUsersBatch(idsStr);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
        }

        return new HashMap<>();
    }

    @Override
    public void saveContent(ContentDTO dto) {
        Content content = convertToEntity(dto);
        content.setUserId(getCurrentUserId());

        try {
            if (dto.getContentId() == null || dto.getContentId().trim().isEmpty()) {
                // 新增：生成唯一ID和设置创建时间
                content.setContentId(UUID.randomUUID().toString());
                content.setCreateTime(LocalDateTime.now());
                contentMapper.insert(content);
            } else {
                // 更新：使用现有ID
                content.setContentId(dto.getContentId());
                content.setCreateTime(LocalDateTime.now());
                contentMapper.updateById(content);
            }
            cleanUnusedImages(dto.getUploadedImages(), dto.getUsedImages());
        } catch (Exception e) {
            throw new RuntimeException("保存内容失败: " + e.getMessage(), e);
        }
    }
    private void cleanUnusedImages(List<String> uploadedImages, List<String> usedImages) {
        if (uploadedImages == null || uploadedImages.isEmpty()) return;
        // 找出未使用的图片
        List<String> unusedImages = uploadedImages.stream()
                .filter(url -> !usedImages.contains(url))
                .toList();
        // 删除未使用的图片
        for (String imageUrl : unusedImages) {
            try {
                boolean deleted = deleteFile(imageUrl);
                if (!deleted) {
                    System.err.println("删除" + imageUrl+"失败");
                }
            } catch (Exception e) {
                // 记录错误但不中断流程
                System.err.println("删除未使用图片失败: " + imageUrl + ", 原因: " + e.getMessage());
            }
        }
    }
    @Override
    public boolean deleteFile(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        String filePath = uploadDir + File.separator + fileName;

        File file = new File(filePath);
        return file.exists() && file.delete();
    }

    @Override
    public void updateContentState(String id, String state) {
        if (id == null || id.isBlank() || "undefined".equals(id) || "null".equals(id)) {
            throw new IllegalArgumentException("无效的内容ID");
        }
        try {
            UpdateWrapper<Content> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id)
                    .eq("uid", getCurrentUserId()) // 仅更新当前用户的内容
                    .set("state",state); // 直接在UpdateWrapper中设置要更新的字段
            contentMapper.update(null, updateWrapper);
        } catch (Exception e) {
            throw new RuntimeException("更新内容失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteContent(String id) { // 软删除：将状态改为delete
        if (id == null || id.isBlank() || "undefined".equals(id) || "null".equals(id)) {
            throw new IllegalArgumentException("无效的内容ID");
        }
        try {
            UpdateWrapper<Content> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id)
                    .eq("uid", getCurrentUserId()) // 仅更新当前用户的内容
                    .set("state", "delete"); // 直接在UpdateWrapper中设置要更新的字段
            contentMapper.update(null, updateWrapper);
        } catch (Exception e) {
            throw new RuntimeException("删除内容失败: " + e.getMessage(), e);
        }
    }


    @Override
    public String uploadFile(MultipartFile file) {
        try {
            // 检查文件是否为空
            String extension = getString(file);
            String uniqueFileName = UUID.randomUUID() + extension;
            String filePath = uploadDir + File.separator + uniqueFileName;

            // 复制文件
            Files.copy(file.getInputStream(), new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);

            return urlPath + "/" + uniqueFileName;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("未知错误: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateLikes(String contentId, int increment) {
        UpdateWrapper<Content> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", contentId)
                .setSql("likes = likes + " + increment);
        contentMapper.update(null, updateWrapper);
    }

    private String getString(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }

        // 创建上传目录
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            boolean created = uploadDirFile.mkdirs();
            if (!created) {
                throw new RuntimeException("无法创建上传目录: " + uploadDir);
            }
        }

        // 验证文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("文件名无效");
        }

        // 检查文件扩展名
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("文件缺少扩展名");
        }

        return originalFilename.substring(lastDotIndex);
    }


    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof JwtUserDTO) {
                // 从JwtUserDTO获取用户ID
                return ((JwtUserDTO) principal).getUid();
            } else {
                throw new SecurityException("用户信息格式不正确");
            }
        }
        throw new SecurityException("用户未认证");
    }

    private ContentDTO convertToDTO(Content content) {
        ContentDTO dto = new ContentDTO();
        dto.setContentId(content.getContentId());
        dto.setUserId(content.getUserId());
        dto.setContent(content.getContent());
        dto.setState(content.getState());
        dto.setCreateTime(content.getCreateTime());
        dto.setLikes(content.getLikes());

        try {
            // 获取评论（第一页，前10条）
            Map<String, Object> commentsResult = interactionFeign
                    .getCommentsByContentId(content.getContentId(), 1, 10)
                    .getData();

            if (commentsResult != null && commentsResult.get("comments") != null) {
                @SuppressWarnings("unchecked")
                List<CommentDTO> comments = (List<CommentDTO>) commentsResult.get("comments");
                dto.setComments(comments);
            } else {
                dto.setComments(List.of());
            }

            // 检查当前用户是否点赞
            Boolean isLiked = interactionFeign
                    .checkIsLiked(content.getContentId(), getCurrentUserId())
                    .getData();
            dto.setIsLiked(isLiked != null ? isLiked : false);
        } catch (Exception e) {
            // 如果调用失败，设置为默认值
            dto.setComments(List.of());
            dto.setIsLiked(false);
            // 记录错误但不中断流程
            System.err.println("调用interaction服务失败: " + e.getMessage());
        }

        return dto;
    }

    private Content convertToEntity(ContentDTO dto) {
        Content content = new Content();
        content.setContentId(dto.getContentId());
        content.setUserId(dto.getUserId());
        content.setContent(dto.getContent());
        content.setState(dto.getState());
        content.setCreateTime(dto.getCreateTime());
        return content;
    }
}