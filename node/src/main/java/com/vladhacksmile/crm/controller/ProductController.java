package com.vladhacksmile.crm.controller;

import com.vladhacksmile.crm.dto.ProductDTO;
import com.vladhacksmile.crm.utils.ResponseMapper;
import com.vladhacksmile.crm.jdbc.user.User;
import com.vladhacksmile.crm.model.result.Result;
import com.vladhacksmile.crm.model.result.SearchResult;
import com.vladhacksmile.crm.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Result<ProductDTO>> createProduct(@AuthenticationPrincipal User authUser, @RequestBody ProductDTO productDTO) {
        return ResponseMapper.map(productService.createProduct(authUser, productDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<ProductDTO>> getProduct(@AuthenticationPrincipal User authUser, @PathVariable Long id) {
        return ResponseMapper.map(productService.getProduct(authUser, id));
    }

    @GetMapping
    public ResponseEntity<Result<SearchResult<ProductDTO>>> getProduct(@AuthenticationPrincipal User authUser,
                                                                       @RequestParam(name = "page_num", defaultValue = "1") int pageNum,
                                                                       @RequestParam(name = "page_size", defaultValue = "10") int pageSize,
                                                                       @RequestParam(name = "sort_type", defaultValue = "ASC") String sortType,
                                                                       @RequestParam(value = "sort_column", defaultValue = "ID") String sortColumn,
                                                                       @RequestParam(value = "filter_operation", defaultValue = "") String filterOperation,
                                                                       @RequestParam(value = "filter_field", defaultValue = "") String filterField,
                                                                       @RequestParam(value = "filter_value", defaultValue = "") String filterValue) {
        return ResponseMapper.map(productService.getAll(authUser, pageNum, pageSize, sortType, sortColumn, filterOperation, filterField, filterValue));
    }

    @PutMapping
    public ResponseEntity<Result<ProductDTO>> updateProduct(@AuthenticationPrincipal User authUser, @RequestBody ProductDTO productDTO) {
        return ResponseMapper.map(productService.updateProduct(authUser, productDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<ProductDTO>> removeProduct(@AuthenticationPrincipal User authUser, @PathVariable Long id) {
        return ResponseMapper.map(productService.removeProduct(authUser, id));
    }

}
