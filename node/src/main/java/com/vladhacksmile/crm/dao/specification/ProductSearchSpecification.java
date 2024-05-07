package com.vladhacksmile.crm.dao.specification;

import com.vladhacksmile.crm.dto.search.SearchCriteria;
import com.vladhacksmile.crm.jdbc.Product;

public class ProductSearchSpecification extends AbstractSearchSpecification<Product> {

    public ProductSearchSpecification(SearchCriteria searchCriteria) {
        super(searchCriteria);
    }
}
