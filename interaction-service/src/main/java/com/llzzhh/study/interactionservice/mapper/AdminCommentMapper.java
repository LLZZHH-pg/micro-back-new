package com.llzzhh.study.interactionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.llzzhh.study.interactionservice.entity.Comment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper; // 新增
import org.apache.ibatis.annotations.Select; // 新增
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update; // 新增

import java.util.List;

@Mapper // 仅加这一行
public interface AdminCommentMapper extends BaseMapper<Comment> {

    // 适配micro_interaction库的comment表（表名按你的实际情况改）
    @Select("SELECT * FROM comment")
    List<Comment> selectAllComments();

    @Update("UPDATE comment SET commCON = #{newContent} WHERE commID = #{commentId}")
    int updateCommentContentById(
            @Param("commentId") String commentId,  // 对应SQL的#{commentId}
            @Param("newContent") String newContent // 对应SQL的#{newContent}
    );
    @Delete("DELETE FROM comment WHERE commID = #{commentId}")
    int deleteCommentById(String commentId);

    // 关联查询（表名按你的实际结构改，比如content表）
    @Select("SELECT c.* FROM comment c LEFT JOIN content ct ON c.content_id = ct.id")
    List<Comment> selectCommentsWithContent();
}