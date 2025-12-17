package com.llzzhh.study.contentservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import com.llzzhh.study.contentservice.service.ContentService;
import com.llzzhh.study.dto.ContentDTO;
import com.llzzhh.study.entity.Comment;
import com.llzzhh.study.contentservice.entity.Content;
import com.llzzhh.study.entity.Like;
import com.llzzhh.study.entity.User;
import com.llzzhh.study.mapper.CommentMapper;
import com.llzzhh.study.contentservice.mapper.ContentMapper;
import com.llzzhh.study.mapper.LikeMapper;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentMapper contentMapper;
    private final LikeMapper likeMapper;
    private final CommentMapper commentMapper;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url-path}")
    private String urlPath;

    @Override
    public List<ContentDTO> getContentsOrdered(int page, int size) {
        int offset = (page - 1) * size;
        List<Content> contents = contentMapper.selectContentWithUsername(
                getCurrentUserId(), offset, size
        );
        return contents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
//        Content content = new Content();
        if (id == null || id.isBlank() || "undefined".equals(id) || "null".equals(id)) {
            throw new IllegalArgumentException("无效的内容ID");
        }
//        content.setId(id);
//        content.setState(state);
        // 条件更新：仅更新当前用户的内容
//        UpdateWrapper<Content> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.eq("id", id)
//                .eq("uid", getCurrentUserId());
//        contentMapper.update(content, updateWrapper);
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
    public void likeContent(String id) {
        if (id == null || id.isBlank() || "undefined".equals(id) || "null".equals(id)) {
            throw new IllegalArgumentException("无效的内容ID");
        }
        try {
            Like like = new Like();
            boolean isLike=isLike(id);
            if (!isLike) {
                like.setLikeId("like"+id + "_" + getCurrentUserId());
                like.setContentId(id);
                like.setUserId(getCurrentUserId());
                like.setCreateTime(LocalDateTime.now());
                likeMapper.insert(like);

                UpdateWrapper<Content> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", id)
                        .setSql("likes = likes + 1");
                contentMapper.update(null, updateWrapper);
            } else {
                String likeId = "like"+id + "_" + getCurrentUserId();
                likeMapper.deleteById(likeId);
                UpdateWrapper<Content> likeWrapper = new UpdateWrapper<>();
                likeWrapper.eq("id", id)
                        .setSql("likes = likes - 1");
                contentMapper.update(null, likeWrapper);
            }
        } catch (Exception e) {
            throw new RuntimeException("点赞内容失败: " + e.getMessage(), e);
        }
    }
    private boolean isLike(String id){
        return likeMapper.exists(new QueryWrapper<Like>()
                .eq("likeID", "like"+id + "_" + getCurrentUserId()));
    }
    @Override
    public void commentContent(String id, String commentText) {
        if (id == null || id.isBlank() || "undefined".equals(id) || "null".equals(id)) {
            throw new IllegalArgumentException("无效的内容ID");
        }
        if (commentText == null || commentText.isBlank()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if(commentText.length()>520){
            throw new IllegalArgumentException("评论内容过长，不能超过520个字符");
        }
        try{
            Comment comment = new Comment();
            comment.setCommentId("comment" + UUID.randomUUID());
            comment.setContentId(id);
            comment.setUserId(getCurrentUserId());
            comment.setCommentText(commentText);
            comment.setCreateTime(LocalDateTime.now());
            commentMapper.insert(comment);
        }catch (Exception e){
            throw new RuntimeException("评论内容失败: " + e.getMessage(), e);
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
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof User currentUser) {
            // 直接从User对象获取uid
            return currentUser.getUid();
        }
        throw new SecurityException("用户未认证");
    }

    private ContentDTO convertToDTO(Content content) {
        ContentDTO dto = new ContentDTO();
        dto.setContentId(content.getContentId());
        dto.setUserId(content.getUserId());
        dto.setUsername(content.getUsername());
        dto.setContent(content.getContent());
        dto.setState(content.getState());
        dto.setCreateTime(content.getCreateTime()); // 修正字段名
        dto.setLikes(content.getLikes());
        dto.setIsLiked(isLike(content.getContentId()));
        List<Comment> comments = commentMapper.selectCommentsWithUsername(content.getContentId());
        dto.setComments(comments);
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