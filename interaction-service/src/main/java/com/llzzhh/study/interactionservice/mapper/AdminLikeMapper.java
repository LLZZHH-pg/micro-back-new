package com.llzzhh.study.interactionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.llzzhh.study.interactionservice.entity.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface AdminLikeMapper extends BaseMapper<Like> {

    @Select("SELECT likeId, contentId, userID, like_createtime FROM likes")
    List<Like> selectAllLikes();

    @Select("SELECT likeId, contentId, userID, like_createtime FROM likes WHERE contentId = #{contentId}")
    List<Like> selectLikesByContentId(@Param("contentId") String contentId);

    @Delete("DELETE FROM likes WHERE likeID = #{likeId}")
    int deleteLikeById(@Param("likeId") String likeId);

    @Select("SELECT " +
            "l.likeId, l.contentId, l.userID, l.like_createtime, " +
            "ct.id AS content_table_id, ct.content AS content_text " +
            "FROM likes l " +
            "LEFT JOIN micro_content.content ct ON l.contentId = ct.id")
    List<Like> selectLikesWithContent();
}