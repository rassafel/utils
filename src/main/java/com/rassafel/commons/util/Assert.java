package com.rassafel.commons.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

@UtilityClass
public class Assert {
    public void isFalse(boolean state, String message) {
        if (state) {
            throw new IllegalArgumentException(message);
        }
    }

    public void isFalse(boolean state, Supplier<String> messageSupplier) {
        if (state) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public void isTrue(boolean state, String message) {
        if (!state) {
            throw new IllegalArgumentException(message);
        }
    }

    public void isTrue(boolean state, Supplier<String> messageSupplier) {
        if (!state) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public void notNull(Object object, Supplier<String> messageSupplier) {
        if (object == null) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    public void isNull(Object object, Supplier<String> messageSupplier) {
        if (object != null) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public void notEmpty(Object[] array, String message) {
        if (ObjectUtils.isEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    public void notEmpty(Object[] array, Supplier<String> messageSupplier) {
        if (ObjectUtils.isEmpty(array)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public void hasSize(Object[] array, int size, String message) {
        val arraySize = array == null ? 0 : array.length;
        if (arraySize != size) {
            throw new IllegalArgumentException(message);
        }
    }

    public void hasSize(Object[] array, int size, Supplier<String> messageSupplier) {
        val arraySize = array == null ? 0 : array.length;
        if (arraySize != size) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    public void notEmpty(Collection<?> collection, Supplier<String> messageSupplier) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public void hasSize(Collection<?> collection, int size, String message) {
        val collectionSize = collection == null ? 0 : collection.size();
        if (collectionSize != size) {
            throw new IllegalArgumentException(message);
        }
    }

    public void hasSize(Collection<?> collection, int size, Supplier<String> messageSupplier) {
        val collectionSize = collection == null ? 0 : collection.size();
        if (collectionSize != size) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public void notEmpty(Map<?, ?> map, String message) {
        if (MapUtils.isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }
    }

    public void notEmpty(Map<?, ?> map, Supplier<String> messageSupplier) {
        if (MapUtils.isEmpty(map)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public void hasSize(Map<?, ?> map, int size, String message) {
        val mapSize = map == null ? 0 : map.size();
        if (mapSize != size) {
            throw new IllegalArgumentException(message);
        }
    }

    public void hasSize(Map<?, ?> map, int size, Supplier<String> messageSupplier) {
        val mapSize = map == null ? 0 : map.size();
        if (mapSize != size) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    public void hasText(String text, String message) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    public void hasText(String text, Supplier<String> messageSupplier) {
        if (!StringUtils.isBlank(text)) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    private String nullSafeGet(Supplier<String> messageSupplier) {
        return (messageSupplier != null ? messageSupplier.get() : null);
    }
}
