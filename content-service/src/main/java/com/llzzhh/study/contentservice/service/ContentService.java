package com.llzzhh.study.contentservice.service;

import com.LLZZHH.study.dto.ContentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContentService {
    List<ContentDTO> getContentsOrdered(int page, int size);
    List<ContentDTO> getSquareContents(int page, int size, int userId);

    void saveContent(ContentDTO dto);
    boolean deleteFile(String fileUrl);

    void updateContentState(String contentId, String state);
    void deleteContent(String contentId);
    String uploadFile(MultipartFile file);

    void updateLikes(String contentId, int increment);
}
