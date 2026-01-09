package com.llzzhh.study.interactionservice.service.impl;

import com.llzzhh.study.interactionservice.entity.Like;
import com.llzzhh.study.interactionservice.mapper.AdminLikeMapper; // 替换为AdminLikeMapper
import com.llzzhh.study.interactionservice.service.AdminLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminLikeServiceImpl implements AdminLikeService {

    // 1. 注入AdminLikeMapper（原LikeMapper替换为AdminLikeMapper）
    private final AdminLikeMapper adminLikeMapper;

    @Override
    public List<Like> getAllLikes() {
        // 2. 调用AdminLikeMapper的selectAllLikes（原likeMapper.selectAll()替换）
        return adminLikeMapper.selectAllLikes();
    }

    @Override
    public List<Like> getLikesByContentId(String contentId) {
        // 3. 调用AdminLikeMapper的selectLikesByContentId（原likeMapper.selectByContentId()替换）
        return adminLikeMapper.selectLikesByContentId(contentId);
    }

    @Override
    public List<Like> getLikesWithContent() {
        // 4. 调用AdminLikeMapper的selectLikesWithContent（原likeMapper.selectLikesWithContent()替换）
        return adminLikeMapper.selectLikesWithContent();

    }
    @Override
    public boolean deleteLikeById(String likeId) {
        // 调用Mapper删除（需在AdminLikeMapper中添加对应的SQL）
        int affectedRows = adminLikeMapper.deleteLikeById(likeId);
        return affectedRows > 0; // 影响行数>0表示删除成功
    }
}