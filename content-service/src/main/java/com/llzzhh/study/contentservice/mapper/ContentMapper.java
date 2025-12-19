package com.llzzhh.study.contentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.llzzhh.study.contentservice.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ContentMapper extends BaseMapper<Content> {
    @Select("SELECT " +
            "c.id AS contentId, " + // 显式设置别名
            "c.uid AS userId, " +
            "c.content, " +
            "c.time AS createTime, " +
            "c.state, " +
            "c.likes " +
            "FROM content c " +
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
            "c.likes " +
            "FROM content c " +
            "WHERE c.state IN ('public') " +
            "ORDER BY c.likes DESC, c.time DESC " +
            "LIMIT #{offset}, #{size}")
    List<Content> selectSquareContentsWithUsername(
            @Param("offset") int offset,
            @Param("size") int size
    );

    @Update("UPDATE content SET likes = likes + #{increment} WHERE id = #{contentId}")
    int updateLikes(@Param("contentId") String contentId, @Param("increment") int increment);
}
