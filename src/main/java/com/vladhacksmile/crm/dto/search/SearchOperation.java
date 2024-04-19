package com.vladhacksmile.crm.dto.search;

import lombok.Getter;

@Getter
public enum SearchOperation {

    LIKE("like"),

    EQUAL("equal"),

    GREATER("greater"),

    LESS("less"),

    GREATER_OR_EQUAL("greater_or_equal"),

    LESS_OR_EQUAL("less_or_equal");

    private final String name;

    SearchOperation(String name) {
        this.name = name;
    }

    public static SearchOperation find(String name) {
        for (SearchOperation operation: values()) {
            if (name.equalsIgnoreCase(operation.getName())) {
                return operation;
            }
        }
        return null;
    }
}
