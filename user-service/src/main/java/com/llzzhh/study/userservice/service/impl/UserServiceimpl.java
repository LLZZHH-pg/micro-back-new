package com.llzzhh.study.userservice.service.impl;

import com.LLZZHH.study.dto.JwtUserDTO;
import com.LLZZHH.study.dto.LoginDTO;
import com.LLZZHH.study.dto.RegisterDTO;
import com.llzzhh.study.userservice.entity.User;
import com.llzzhh.study.userservice.mapper.UserMapper;
import com.llzzhh.study.userservice.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder; // 必须导入！
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceimpl implements UserService {

    // ========== 原有依赖：保留 ==========
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    // ========== 关键修复：声明+注入PasswordEncoder（解决无法解析） ==========
    // 1. 必须用private final声明（配合@RequiredArgsConstructor自动注入）
    // 2. 必须导入org.springframework.security.crypto.password.PasswordEncoder包
    private final PasswordEncoder passwordEncoder;

    // ========== 注册方法：加密密码（核心） ==========
    @Override
    public String register(RegisterDTO dto) {
        // 校验加密器是否注入（可选，用于排查）
        if (passwordEncoder == null) {
            throw new RuntimeException("密码加密器注入失败！");
        }

        // 原有参数校验：保留
        validateRegister(dto);

        // 原有唯一性校验：保留
        if (exists(User::getEmail, dto.getEmail())) {
            throw new RuntimeException("该邮箱已被注册");
        }
        if (exists(User::getTel, dto.getTel())) {
            throw new RuntimeException("该手机号已被注册");
        }
        if (exists(User::getName, dto.getName())) {
            throw new RuntimeException("该用户名已被注册");
        }

        // 封装用户：密码加密（核心修改）
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setTel(dto.getTel());
        user.setName(dto.getName());
        // ✅ 加密密码后存入，解决明文问题
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userMapper.insert(user);

        return String.format("%06d", user.getUid());
    }

    // ========== 登录方法：密文比对（核心） ==========
    @Override
    public String login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        String account = dto.getAccount();

        // 原有账号类型判断：保留
        if (account.contains("@")) {
            wrapper.eq(User::getEmail, account);
        } else if (account.matches("\\d{11}")) {
            wrapper.eq(User::getTel, account);
        } else {
            wrapper.eq(User::getName, account);
        }

        // ❌ 删除原有明文密码比对（关键！）
        // wrapper.eq(User::getPassword, dto.getPassword());

        // 查询用户：保留
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            boolean accountExists = exists(User::getEmail, account) ||
                    exists(User::getTel, account) ||
                    exists(User::getName, account);
            if (accountExists) {
                throw new RuntimeException("密码错误");
            } else {
                throw new RuntimeException("账号不存在");
            }
        }

        // ✅ 新增：密文密码比对（核心）
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 原有生成JWT：保留
        return generateJwtToken(user);
    }

    // ========== 原有方法：全部保留 ==========
    private String generateJwtToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        JwtUserDTO jwtUser = new JwtUserDTO();
        jwtUser.setUid(user.getUid());
        jwtUser.setName(user.getName());
        jwtUser.setEmail(user.getEmail());
        jwtUser.setRole(user.getRole());

        String jti = UUID.randomUUID().toString();
        String token=Jwts.builder()
                .setId(jti)
                .setSubject(String.valueOf(user.getUid()))
                .claim("user", jwtUser)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
        String redisKey = "auth:token:" + jti;
        try {
            redisTemplate.opsForValue().set(redisKey, token, Duration.ofMillis(jwtExpirationMs));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return token;
    }

    private boolean exists(SFunction<User, ?> column, String val) {
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery().eq(column, val);
        return userMapper.selectCount(wrapper) > 0;
    }

    private void validateRegister(RegisterDTO dto) {
        String email = dto.getEmail();
        String tel   = dto.getTel();
        String name  = dto.getName();
        String pwd   = dto.getPassword();

        if (tel == null || tel.trim().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        if (!tel.matches("\\d{11}")) {
            throw new RuntimeException("电话号码必须是11位数字");
        }
        if (pwd == null || pwd.trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (pwd.length() < 8 || pwd.length() > 16) {
            throw new RuntimeException("密码需要在8~16位之间");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("邮箱不能为空");
        }
        if (!email.matches("^[A-Za-z0-9]+@[A-Za-z0-9]+$")) {
            throw new RuntimeException("邮箱格式不正确");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (name.length() < 2 || name.length() > 10 || !name.matches(".*[a-zA-Z].*")) {
            throw new RuntimeException("用户名在2~10个字符之间，且包含至少一个字母");
        }
    }

    @Override
    public User getUserById(Integer userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public List<User> getUsersByIds(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery().in(User::getUid, userIds);
        return userMapper.selectList(wrapper);
    }
}