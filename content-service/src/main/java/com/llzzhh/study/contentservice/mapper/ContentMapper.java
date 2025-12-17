package com.llzzhh.study.contentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.llzzhh.study.contentservice.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ContentMapper extends BaseMapper<Content> {
    @Select("SELECT " +
            "c.id AS contentId, " + // 显式设置别名
            "c.uid AS userId, " +
            "c.content, " +
            "c.time AS createTime, " +
            "c.state, " +
            "c.likes, " +
            "u.NAM AS username " +
            "FROM content c " +
            "JOIN user_info u ON c.uid = u.UID " +
            "WHERE c.uid = #{userId} " +
            "AND c.state IN ('private','public','save') " +
            "ORDER BY c.time DESC " +
            "LIMIT #{offset}, #{size}")
    List<Content> selectContentWithUsername(@Param("userId") Integer userId,
                                            @Param("offset") int offset,
                                            @Param("size") int size);

    @Select("SELECT " +
            "c.id AS contentId, " +
            "c.uid AS userId, " +
            "c.content, " +
            "c.time AS createTime, " +
            "c.state, " +
            "c.likes, " +
            "u.NAM AS username " +
            "FROM content c " +
            "JOIN user_info u ON c.uid = u.UID " +
            "WHERE c.state IN ('public') " +
            "AND u.STA = '正常' " +
            "ORDER BY c.likes DESC, c.time DESC " +
            "LIMIT #{offset}, #{size}")
    List<Content> selectSquareContentsWithUsername(
            @Param("offset") int offset,
            @Param("size") int size
    );
}
