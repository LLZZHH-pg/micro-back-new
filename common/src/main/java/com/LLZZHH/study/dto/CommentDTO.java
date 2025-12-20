package com.LLZZHH.study.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private String commentId;
    private String contentId;
    private Integer userId;
    private String commentText;
    private LocalDateTime createTime;
    private String username;
}
