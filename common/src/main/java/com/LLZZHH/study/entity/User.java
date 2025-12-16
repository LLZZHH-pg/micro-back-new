package com.llzzhh.study.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("user_info")
public class User {
    @TableId(value = "UID", type = IdType.AUTO)
    private Integer uid;

    @TableField("EML")
    private String email;

    @TableField("TEL")
    private String tel;

    @TableField("NAM")
    private String name;

    @TableField("PAS")
    private String password;
}
