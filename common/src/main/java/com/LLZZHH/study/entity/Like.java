package com.llzzhh.study.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("likes")
public class Like {
    @TableId("likeId")
    private String likeId;
    @TableField("contentId")
    private String contentId;
    @TableField("userID")
    private Integer userId;
    @TableField("like_createtime")
    private LocalDateTime createTime;
}
