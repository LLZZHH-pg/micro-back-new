package com.llzzhh.study.interactionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // 新增这行
import com.llzzhh.study.interactionservice.entity.Like;

import java.util.List;

// 继承BaseMapper<Like>，自动获得insert/deleteById/exists等CRUD方法
public interface LikeMapper extends BaseMapper<Like> {
    // 保留你原有的查询方法（不影响）
    List<Like> selectAll();
    List<Like> selectByContentId(String contentId);
    List<Like> selectLikesWithContent();
}