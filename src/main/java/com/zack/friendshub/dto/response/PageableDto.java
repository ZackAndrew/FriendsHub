package com.zack.friendshub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageableDto<T> {
    private List<T> content;

    private long totalElements;

    private int currentPage;

    private int totalPages;
}
