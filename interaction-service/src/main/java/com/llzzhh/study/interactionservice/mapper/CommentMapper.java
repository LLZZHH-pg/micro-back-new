// CommentMapper.java
package com.llzzhh.study.interactionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.llzzhh.study.interactionservice.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    // 移除JOIN user_info，只查询评论数据
    @Select("SELECT " +
            "c.commID AS commentId, " +
            "c.contentID AS contentId, " +
            "c.userID AS userId, " +
            "c.commCON AS commentText, " +
            "c.comment_createtime AS createTime " +
            "FROM comment c " +
            "WHERE c.contentID = #{contentId} " +
            "ORDER BY c.comment_createtime DESC " +
            "LIMIT #{offset}, #{size}")
    List<Comment> selectCommentsByContentId(
            @Param("contentId") String contentId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    // 查询评论总数
    @Select("SELECT COUNT(*) FROM comment WHERE contentID = #{contentId}")
    int countCommentsByContentId(@Param("contentId") String contentId);

    // 批量查询评论（用于Feign调用）
    @Select("SELECT " +
            "c.commID AS commentId, " +
            "c.contentID AS contentId, " +
            "c.userID AS userId, " +
            "c.commCON AS commentText, " +
            "c.comment_createtime AS createTime " +
            "FROM comment c " +
            "WHERE c.contentID = #{contentId} " +
            "ORDER BY c.comment_createtime DESC")
    List<Comment> selectAllCommentsByContentId(@Param("contentId") String contentId);
}
