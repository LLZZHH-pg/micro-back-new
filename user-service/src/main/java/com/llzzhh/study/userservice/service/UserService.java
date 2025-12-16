package com.llzzhh.study.userservice.service;

import com.llzzhh.study.dto.LoginDTO;
import com.llzzhh.study.dto.RegisterDTO;

public interface UserService {
    String register(RegisterDTO dto);
    String login(LoginDTO dto);
}
