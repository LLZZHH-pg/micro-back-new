package com.llzzhh.study.userservice.service;

import com.llzzhh.study.dto.LoginDTO;
import com.llzzhh.study.dto.RegisterDTO;
import com.llzzhh.study.userservice.entity.User;

import java.util.List;

public interface UserService {
    String register(RegisterDTO dto);
    String login(LoginDTO dto);
    User getUserById(Integer userId);
    List<User> getUsersByIds(List<Integer> userIds);
}
