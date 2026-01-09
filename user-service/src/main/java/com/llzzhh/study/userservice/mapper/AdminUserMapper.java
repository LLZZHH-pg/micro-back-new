package com.llzzhh.study.userservice.mapper; // 注意：是mapper下的admin子包

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.llzzhh.study.userservice.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdminUserMapper extends BaseMapper<User> {

    @Select("SELECT UID, TEL, EML, NAM, PAS, STA, ROL FROM user")
    List<User> selectAllUsers();

    @Update("UPDATE user SET STA = #{newState} WHERE UID = #{userId}")
    int updateUserStateById(@Param("userId") Integer userId, @Param("newState") String newState);

    @Select("SELECT UID, TEL, EML, NAM, PAS, STA, ROL FROM user WHERE ROL = #{role}")
    List<User> selectUsersByRole(@Param("role") String role);

    @Update("UPDATE user SET ROL = #{newRole} WHERE UID = #{userId}")
    int updateUserRoleById(@Param("userId") Integer userId, @Param("newRole") String newRole);
}