package com.llzzhh.study.userservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("user")
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

    @TableField("ROL")
    private String role;

    @TableField("STA")
    private String state;
}
