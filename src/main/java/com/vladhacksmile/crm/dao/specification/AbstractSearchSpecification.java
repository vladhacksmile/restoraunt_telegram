package com.vladhacksmile.crm.dao.specification;

import com.vladhacksmile.crm.dto.search.SearchCriteria;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public abstract class AbstractSearchSpecification<T> implements Specification<T> {

    private final SearchCriteria searchCriteria;

    public AbstractSearchSpecification(SearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        if (searchCriteria.getSearchOperation() != null && StringUtils.isNotEmpty(searchCriteria.getObject())
                && StringUtils.isNotEmpty(searchCriteria.getValue())) {
            String lowerObject = searchCriteria.getObject().toLowerCase();
            String lowerValue = searchCriteria.getValue().toLowerCase();
            switch (searchCriteria.getSearchOperation()) {
                case GREATER -> {
                    return criteriaBuilder.greaterThan(root.get(lowerObject), lowerValue);
                }
                case LESS -> {
                    return criteriaBuilder.lessThan(root.get(lowerObject), lowerValue);
                }
                case LIKE -> {
                    return criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get(lowerObject)
                            ), "%" + lowerValue + "%"
                    );
                }
                case EQUAL -> {
                    return criteriaBuilder.equal(root.get(lowerObject), lowerValue);
                }
                case GREATER_OR_EQUAL -> {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get(lowerObject), lowerValue);
                }
                case LESS_OR_EQUAL -> {
                    return criteriaBuilder.lessThanOrEqualTo(root.get(lowerObject), lowerValue);
                }
                default -> {
                    return null;
                }
            }
        }
        return null;
    }
}