package com.llzzhh.study.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContentDTO {
    private String contentId;
    private Integer userId;
    private String username;
    private String content;
    private String state;
    private LocalDateTime createTime;
    private Integer likes;
    private Boolean isLiked;

    private String commentText;
    private List<CommentDTO> comments;

    private List<String> uploadedImages;
    private List<String> usedImages;
    private Integer page;
    private Integer pageSize;
}
