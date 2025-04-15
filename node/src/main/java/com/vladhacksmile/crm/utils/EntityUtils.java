package com.vladhacksmile.crm.utils;

import java.util.Objects;
import java.util.function.Consumer;

public class EntityUtils {

    public static <T> void setIfUpdated(Object prevValue, Object newValue, Consumer<T> newValueSetter) {
        if (!Objects.equals(prevValue, newValue)) {
            //noinspection unchecked
            newValueSetter.accept((T) newValue);
        }
    }
}
