package com.llzzhh.study.contentservice.service;

import com.llzzhh.study.dto.ContentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContentService {
    List<ContentDTO> getContentsOrdered(int page, int size);
    List<ContentDTO> getSquareContents(int page, int size);

    void saveContent(ContentDTO dto);
    boolean deleteFile(String fileUrl);

    void updateContentState(String id, String state);
    void deleteContent(String id);
    String uploadFile(MultipartFile file);

    void updateLikes(String id, int increment);
}
