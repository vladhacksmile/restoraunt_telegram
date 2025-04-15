package com.vladhacksmile.crm.service;

import com.vladhacksmile.crm.dto.ProductDTO;
import com.vladhacksmile.crm.jdbc.user.User;
import com.vladhacksmile.crm.model.result.Result;
import com.vladhacksmile.crm.model.result.SearchResult;

public interface ProductService {

    Result<ProductDTO> createProduct(User authUser, ProductDTO productDTO);

    Result<ProductDTO> updateProduct(User authUser, ProductDTO productDTO);

    Result<ProductDTO> removeProduct(User authUser, Long id);

    Result<ProductDTO> getProduct(User authUser, Long id);

    Result<SearchResult<ProductDTO>> getAll(User authUser, int pageNum, int pageSize, String sortType, String sortColumn,
                                            String filterOperation, String filterField, String filterValue);

}
