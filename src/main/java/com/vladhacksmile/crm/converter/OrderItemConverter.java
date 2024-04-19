package com.vladhacksmile.crm.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vladhacksmile.crm.jdbc.OrderItem;
import lombok.extern.log4j.Log4j2;

import javax.persistence.AttributeConverter;
import java.util.Comparator;
import java.util.List;

@Log4j2
public class OrderItemConverter implements AttributeConverter<List<OrderItem>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public String convertToDatabaseColumn(List<OrderItem> orderItems) {
        try {
            orderItems.sort(Comparator.comparing(OrderItem::getPrice));
            return objectMapper.writeValueAsString(orderItems);
        } catch (JsonProcessingException e) {
            log.error("", e);
        }

        return null;
    }

    @Override
    public List<OrderItem> convertToEntityAttribute(String string) {
        try {
            List<OrderItem> orderItems = objectMapper.readValue(string, new TypeReference<>(){});
            orderItems.sort(Comparator.comparing(OrderItem::getPrice));
            return orderItems;
        } catch (JsonProcessingException e) {
            log.error("", e);
        }

        return null;
    }
}
