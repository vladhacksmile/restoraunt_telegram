package com.vladhacksmile.crm.service.impl;

import com.vladhacksmile.crm.dao.ProductDAO;
import com.vladhacksmile.crm.dao.specification.ProductSearchSpecification;
import com.vladhacksmile.crm.dto.ProductDTO;
import com.vladhacksmile.crm.dto.search.SearchCriteria;
import com.vladhacksmile.crm.dto.search.SearchOperation;
import com.vladhacksmile.crm.jdbc.Product;
import com.vladhacksmile.crm.jdbc.user.User;
import com.vladhacksmile.crm.model.result.Result;
import com.vladhacksmile.crm.model.result.SearchResult;
import com.vladhacksmile.crm.service.ProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.vladhacksmile.crm.model.result.Result.resultOk;
import static com.vladhacksmile.crm.model.result.Result.resultWithStatus;
import static com.vladhacksmile.crm.model.result.SearchResult.makeSearchResult;
import static com.vladhacksmile.crm.model.result.status.Status.INCORRECT_PARAMS;
import static com.vladhacksmile.crm.model.result.status.Status.NOT_FOUND;
import static com.vladhacksmile.crm.model.result.status.StatusDescription.*;
import static com.vladhacksmile.crm.utils.EntityUtils.setIfUpdated;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDAO productDAO;

    @Override
    @Transactional
    public Result<ProductDTO> createProduct(User authUser, ProductDTO productDTO) {
        if (productDTO == null) {
            return resultWithStatus(INCORRECT_PARAMS, PRODUCT_IS_NULL);
        }

        if (productDTO.getId() != null) {
            return resultWithStatus(INCORRECT_PARAMS, PRODUCT_ID_IS_NOT_NULL);
        }

       Result<?> validateProductResult = validate(productDTO);
       if (validateProductResult.isError()) {
           return validateProductResult.cast();
       }

       Product product = convert(productDTO);

       productDAO.save(product);

       return resultOk(convert(product));
    }

    @Override
    @Transactional
    public Result<ProductDTO> updateProduct(User authUser, ProductDTO productDTO) {
        if (productDTO == null) {
            return resultWithStatus(INCORRECT_PARAMS, PRODUCT_IS_NULL);
        }

        if (productDTO.getId() == null) {
            return resultWithStatus(INCORRECT_PARAMS, PRODUCT_ID_IS_NULL);
        }

        Result<?> validateProductResult = validate(productDTO);
        if (validateProductResult.isError()) {
            return validateProductResult.cast();
        }

        Long productId = productDTO.getId();
        Product product = productDAO.findById(productId).orElse(null);
        if (product == null || product.getDeletedDate() != null) {
            return resultWithStatus(NOT_FOUND, PRODUCT_NOT_FOUND);
        }

        setIfUpdated(productDTO.getCount(), product.getCount(), product::setCount);
        setIfUpdated(productDTO.getName(), product.getName(), product::setName);
        setIfUpdated(productDTO.getDescription(), product.getDescription(), product::setDescription);
        setIfUpdated(productDTO.getPictureId(), product.getPictureId(), product::setPictureId);
        setIfUpdated(productDTO.getPrice(), product.getPrice(), product::setPrice);
        setIfUpdated(productDTO.getWeight(), product.getWeight(), product::setWeight);
        setIfUpdated(productDTO.getCalories(), product.getCalories(), product::setCalories);
        setIfUpdated(productDTO.getNutritional(), product.getNutritional(), product::setNutritional);
        setIfUpdated(productDTO.getPictureId(), product.getProteins(), product::setProteins);
        setIfUpdated(productDTO.getFats(), product.getFats(), product::setFats);
        productDAO.save(product);

        return resultOk(convert(product));
    }

    @Override
    @Transactional
    public Result<ProductDTO> removeProduct(User authUser, Long id) {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            return resultWithStatus(INCORRECT_PARAMS, PRODUCT_ID_IS_NULL);
        }

        Product product = productDAO.findById(id).orElse(null);
        if (product == null || product.getDeletedDate() != null) {
            return resultWithStatus(NOT_FOUND, PRODUCT_NOT_FOUND);
        }

        product.setDeletedDate(now);

        productDAO.save(product);

        return resultOk(convert(product));
    }

    @Override
    public Result<ProductDTO> getProduct(User authUser, Long id) {
        if (id == null) {
            return resultWithStatus(INCORRECT_PARAMS, PRODUCT_ID_IS_NULL);
        }

        Product product = productDAO.findById(id).orElse(null);
        if (product == null || product.getDeletedDate() != null) {
            return resultWithStatus(NOT_FOUND, PRODUCT_NOT_FOUND);
        }

        return resultOk(convert(product));
    }

    @Override
    public Result<SearchResult<ProductDTO>> getAll(User authUser, int pageNum, int pageSize, String sortType, String sortColumn,
                                                   String filterOperation, String filterField, String filterValue) {
        if (pageNum < 1) {
            return resultWithStatus(INCORRECT_PARAMS, PAGE_NUM_MUST_BE_POSITIVE);
        }

        if (pageSize < 1) {
            return resultWithStatus(INCORRECT_PARAMS, PAGE_SIZE_MUST_BE_POSITIVE);
        }

        if (StringUtils.isEmpty(sortType)) {
            sortType = "ASC";
        }

        if (!sortType.equalsIgnoreCase("ASC") && !sortType.equalsIgnoreCase("DESC")) {
            return resultWithStatus(INCORRECT_PARAMS, INCORRECT_SORT_TYPE);
        }

        SearchOperation searchOperation = null;
        if (StringUtils.isNotEmpty(filterField)) {
            searchOperation = SearchOperation.find(filterOperation);
            if (searchOperation == null) {
                return resultWithStatus(NOT_FOUND, FILTER_OPERATION_NOT_FOUND);
            }
        }

        boolean defaultSort = sortType.equalsIgnoreCase("ASC");
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, StringUtils.isNotEmpty(sortColumn) ?
                Sort.by(defaultSort ? Sort.Direction.ASC : Sort.Direction.DESC, sortColumn) : Sort.unsorted());
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setReverseSort(sortType.equalsIgnoreCase("DESC"));
        searchCriteria.setSearchOperation(searchOperation);
        searchCriteria.setObject(filterField);
        searchCriteria.setValue(filterValue);

        Page<Product> productsPage = productDAO.findAll(new ProductSearchSpecification(searchCriteria), pageable);
        List<Product> products = productsPage.stream().toList();
        if (CollectionUtils.isEmpty(products)) {
            return resultWithStatus(NOT_FOUND, PRODUCT_NOT_FOUND);
        }

        return resultOk(makeSearchResult(products.stream().map(this::convert).collect(Collectors.toList()),
                products.size(), productsPage.getTotalPages(), productsPage.getTotalElements()));
    }

    private Product convert(ProductDTO productDTO) {
        Product product = new Product();
        product.setId(productDTO.getId());
        product.setCount(productDTO.getCount());
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPictureId(productDTO.getPictureId());
        product.setPrice(productDTO.getPrice());
        product.setWeight(productDTO.getWeight());
        product.setCalories(productDTO.getCalories());
        product.setNutritional(productDTO.getNutritional());
        product.setProteins(productDTO.getProteins());
        product.setFats(productDTO.getFats());

        return product;
    }

    private ProductDTO convert(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setCount(product.getCount());
        productDTO.setName(product.getName());
        productDTO.setDescription(product.getDescription());
        productDTO.setPictureId(product.getPictureId());
        productDTO.setPrice(product.getPrice());
        productDTO.setWeight(product.getWeight());
        productDTO.setCalories(product.getCalories());
        productDTO.setNutritional(product.getNutritional());
        productDTO.setProteins(product.getProteins());
        productDTO.setFats(product.getFats());

        return productDTO;
    }

    private <T> Result<T> validate(ProductDTO productDTO) {
        if (productDTO == null) {
            return resultWithStatus(INCORRECT_PARAMS, PRODUCT_IS_NULL);
        }

        if (productDTO.getCount() == null) {
            productDTO.setCount(1L);
        }

        if (StringUtils.isEmpty(productDTO.getName())) {
            return resultWithStatus(INCORRECT_PARAMS, PRODUCT_NAME_IS_NULL);
        }

        if (StringUtils.isEmpty(productDTO.getPictureId())) {
            return resultWithStatus(INCORRECT_PARAMS, PRODUCT_PICTURE_ID_IS_NULL);
        }

        if (productDTO.getPrice() == null) {
            return resultWithStatus(INCORRECT_PARAMS, PRODUCT_PRICE_IS_NULL);
        }

        if (productDTO.getWeight() == null) {
            return resultWithStatus(INCORRECT_PARAMS, PRODUCT_WEIGHT_IS_NULL);
        }

        return resultOk();
    }
}
