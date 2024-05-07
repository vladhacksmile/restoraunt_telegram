package com.vladhacksmile.crm.model.result;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SearchResult<T> extends Result<T> {

    private List<T> objects;

    private Integer pageSize;

    private Integer pageNum;

    private Integer pageTotal;

    private Long totalElements;

    public static <T> SearchResult<T> makeSearchResult(List<T> objects, Integer pageNum, Integer pageTotal, Long totalElements) {
        return new SearchResult<>(objects, objects.size(), pageNum, pageTotal, totalElements);
    }
}
