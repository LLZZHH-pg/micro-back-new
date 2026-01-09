package com.llzzhh.study.userservice.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置类 - 全局唯一
 * BCrypt加密器，Spring管理这个Bean，后面在Service里直接注入使用
 */
@Configuration
public class PasswordConfig {

    // 注入BCrypt密码加密器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}