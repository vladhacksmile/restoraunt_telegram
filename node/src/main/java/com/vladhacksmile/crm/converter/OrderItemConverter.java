package com.vladhacksmile.crm.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vladhacksmile.crm.jdbc.order.OrderItem;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collections;
import java.util.List;

@Log4j2
@Converter
@Component
public class OrderItemConverter implements AttributeConverter<List<OrderItem>, String> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(List<OrderItem> orderItems) {
        if (CollectionUtils.isEmpty(orderItems)) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(orderItems);
        } catch (JsonProcessingException e) {
            log.error("", e);
        }

        return null;
    }

    @Override
    public List<OrderItem> convertToEntityAttribute(String string) {
        if (StringUtils.isEmpty(string)) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(string, new TypeReference<>(){});
        } catch (JsonProcessingException e) {
            log.error("", e);
        }

        return null;
    }
}
