package com.LLZZHH.study.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class JwtUserDTO implements Serializable {
    private Integer uid;
    private String name;
    private String email;
    private String role;

}
