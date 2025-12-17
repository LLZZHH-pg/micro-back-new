package com.llzzhh.study.squareservice.service;


import com.llzzhh.study.dto.ContentDTO;
import java.util.List;

public interface SquareService {
    List<ContentDTO> getContentsOrderedSquare(int page, int size);

}
