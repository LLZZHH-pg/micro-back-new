package com.llzzhh.study.contentservice.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("content")
public class Content {
    @TableId(value = "id")
    private String contentId;

    @TableField("uid")
    private Integer userId;

    private String content;
    private String state;

    @TableField("time")
    private LocalDateTime createTime;

    private Integer likes;

    @TableField(exist = false)
    private String username; // 非数据库字段
}
