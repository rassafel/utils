/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rassafel.commons.util;

import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@UtilityClass
public class StreamUtils {
    /**
     * Create stream from iterable, if iterable is null return empty stream.
     *
     * @param iterable iterable
     * @param <T>      item
     * @return items stream
     */
    public static <T> Stream<T> emptyIfNull(Iterable<T> iterable) {
        if (iterable == null) return Stream.empty();
        if (iterable instanceof Collection<T> collection) {
            if (collection.isEmpty()) return Stream.empty();
            return collection.stream();
        }
        var iterator = iterable.iterator();
        if (!iterator.hasNext()) return Stream.empty();
        return StreamSupport.stream(iterable.spliterator(), false);
    }


    /**
     * Create stream from array, if array is null return empty stream.
     *
     * @param array array
     * @param <T>   item
     * @return items stream
     */
    public static <T> Stream<T> emptyIfNull(T[] array) {
        if (array == null || array.length == 0) return Stream.empty();
        return Arrays.stream(array);
    }

    /**
     * Create stream from Map, if map is null return empty stream.
     *
     * @param map map
     * @param <K> key
     * @param <V> value
     * @return items stream
     */
    public static <K, V> Stream<Map.Entry<K, V>> emptyIfNull(Map<K, V> map) {
        if (map == null || map.isEmpty()) return Stream.empty();
        return map.entrySet().stream();
    }

    /**
     * Distinct items by key, pass nulls
     *
     * @param keyExtractor key extractor
     * @param <T>          item
     * @return distinct predicate
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        return distinctByKey(keyExtractor, true);
    }

    /**
     * Distinct items by key
     *
     * @param keyExtractor key extractor
     * @param passNull     pass nulls if true, skip nulls if false
     * @param <T>          item
     * @return distinct predicate
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor, boolean passNull) {
        Assert.notNull(keyExtractor, "keyExtractor must not be null");
        var seen = new ConcurrentHashMap<Object, Boolean>();
        return t -> {
            if (t == null) return passNull;
            var key = keyExtractor.apply(t);
            if (key == null) return passNull;
            return seen.putIfAbsent(key, Boolean.TRUE) == null;
        };
    }

    /**
     * Map value if not null
     *
     * @param function mapping function
     * @param <S>      input type
     * @param <R>      output type
     * @return composed function skip null mapping
     */
    public static <S, R> R mapIfNotNull(@Nullable S source, Function<S, R> function) {
        Objects.requireNonNull(function);
        return source == null ? null : function.apply(source);
    }

    /**
     * Map value if not null
     *
     * @param function mapping function
     * @param <S>      input type
     * @param <R>      output type
     * @return composed function skip null mapping
     */
    public static <S, R> Function<S, R> mapIfNotNull(Function<S, R> function) {
        Objects.requireNonNull(function);
        return s -> s == null ? null : function.apply(s);
    }

    /**
     * Collect Entry stream to Map
     *
     * @param <K> entry key
     * @param <V> entry value
     * @return to map collector
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    /**
     * Collect stream to LinkedHashSet
     *
     * @param <T> item
     * @return to set collector
     */
    public static <T> Collector<T, ?, Set<T>> toSet() {
        return Collectors.toCollection(LinkedHashSet::new);
    }

    /**
     * Return exactly one item.
     * Throw exception if stream is empty or contains more that one item
     *
     * @param <T> item
     * @return item
     */
    public static <T> Collector<T, ?, T> exactlyOne() {
        return exactlyOne(IllegalArgumentException::new);
    }

    /**
     * Return exactly one item.
     * Throw exception if stream is empty or contains more that one item
     *
     * @param <T> item
     * @param <X> exception
     * @return item
     */
    public static <T, X extends RuntimeException> Collector<T, ?, T> exactlyOne(Supplier<X> exceptionSupplier) throws X {
        class ValueHolder implements Consumer<T> {
            T value = null;
            boolean present = false;

            @Override
            public void accept(T t) {
                if (present) {
                    throw exceptionSupplier.get();
                }
                value = t;
                present = true;
            }
        }
        return Collector.of(ValueHolder::new, ValueHolder::accept, (h1, h2) -> {
            if (h2.present) {
                h1.accept(h2.value);
            }
            return h1;
        }, holder -> {
            if (holder.present) return holder.value;
            throw exceptionSupplier.get();
        });
    }
}
