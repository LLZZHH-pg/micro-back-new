package com.llzzhh.study.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String account;   // 邮箱 / 手机号 / 用户名
    private String password;
}
