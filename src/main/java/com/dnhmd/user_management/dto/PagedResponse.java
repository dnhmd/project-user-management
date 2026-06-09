package com.dnhmd.user_management.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private boolean isFirst;
    private boolean isLast;
}
