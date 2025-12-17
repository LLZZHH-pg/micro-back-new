package com.llzzhh.study.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.llzzhh.study.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    @Select("SELECT " +
            "c.commID AS commentId, " +
            "c.contentID AS contentId, " +
            "c.userID AS userId, " +
            "c.commCON AS commentText, " +
            "c.comment_createtime AS createTime, " +
            "u.NAM AS username " +
            "FROM comment c " +
            "JOIN user_info u ON c.userID = u.UID " +
            "WHERE c.contentID = #{contentId} " +
            "ORDER BY c.comment_createtime DESC")
    List<Comment> selectCommentsWithUsername(@Param("contentId") String contentId);
}
