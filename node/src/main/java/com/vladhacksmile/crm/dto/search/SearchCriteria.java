package com.vladhacksmile.crm.dto.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SearchCriteria {

    private boolean reverseSort;

    private SearchOperation searchOperation;

    private String object;

    private String value;

}
