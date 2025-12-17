package com.llzzhh.study.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId("commID")
    private String commentId;

    @TableField("contentID")
    private String contentId;

    @TableField("userID")
    private Integer userId;

    @TableField("comment_createtime")
    private LocalDateTime createTime;

    @TableField("commCON")
    private String commentText;

    @TableField(exist = false)
    private String username; // 非数据库字段
}
