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

package io.github.rassafel.commons.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

@UtilityClass
public class StreamUtils {
    /**
     * Create stream from iterable, if iterable is null return empty stream.
     *
     * @param iterable iterable
     * @param <T>      item
     * @return items stream
     */
    public static <T> Stream<T> emptyIfNull(@Nullable Iterable<T> iterable) {
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
    public static <T> Stream<T> emptyIfNull(@Nullable T[] array) {
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
    public static <K, V> Stream<Map.Entry<K, V>> emptyIfNull(@Nullable Map<K, V> map) {
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
    public static <T> Predicate<T> distinctByKey(@NonNull Function<? super T, ?> keyExtractor, boolean passNull) {
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
    @Nullable
    public static <S, R> R mapIfNotNull(@Nullable S source, @NonNull Function<S, R> function) {
        if (source == null) {
            return null;
        }
        return function.apply(source);
    }

    /**
     * Map value if not null
     *
     * @param function mapping function
     * @param <S>      input type
     * @param <R>      output type
     * @return composed function skip null mapping
     */
    public static <S, R> Function<S, R> mapIfNotNull(@NonNull Function<S, R> function) {
        return s -> s == null ? null : function.apply(s);
    }

    /**
     * Filter and cast stream element to specific type.
     * <p>
     * Use in {@link Stream#flatMap(Function)}.
     * If stream element is not instance of target class, it will be ignored.
     *
     * @param clazz target class type
     * @param <T>   input type
     * @param <R>   output type
     * @return composed function filter and cast stream element to specific type.
     */
    public static <T, R> Function<T, Stream<R>> filterAndCast(Class<R> clazz) {
        return t -> {
            if (clazz.isInstance(t)) {
                return Stream.of(clazz.cast(t));
            }
            return Stream.empty();
        };
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
     * Collect stream to Map with key extractor
     *
     * @param keyExtractor key extractor
     * @param <K>          key type
     * @param <V>          value type
     * @return to map collector
     * @throws IllegalStateException if key extractor returns duplicate keys
     */
    public static <K, V> Collector<V, ?, Map<K, V>> toMap(Function<V, K> keyExtractor) {
        return Collectors.toMap(keyExtractor, Function.identity());
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
    public static <T, X extends RuntimeException> Collector<T, ?, T> exactlyOne(@NonNull Supplier<X> exceptionSupplier) {
        return Collector.<T, ValueHolder<T, X>, T>of(
                () -> new ValueHolder<>(exceptionSupplier),
                ValueHolder::accept,
                (h1, h2) -> {
                    if (h2.isPresent()) {
                        h1.accept(h2.getValue());
                    }
                    return h1;
                },
                holder -> {
                    if (holder.isPresent()) return holder.getValue();
                    throw exceptionSupplier.get();
                });
    }

    /**
     * Count items and collect min max value.
     *
     * @param <T> item
     * @return Stats object
     */
    public static <T extends Comparable<? super T>> Collector<T, ?, Stats<T>> stats() {
        return stats(Comparator.naturalOrder());
    }

    /**
     * Count items and collect min max value.
     *
     * @param comparator comparator
     * @param <T>        item
     * @return Stats object
     */
    public static <T> Collector<T, Stats<T>, Stats<T>> stats(Comparator<? super T> comparator) {
        return Collector.of(
                () -> new Stats<>(comparator),
                Stats::accept,
                Stats::combine,
                Collector.Characteristics.UNORDERED, Collector.Characteristics.IDENTITY_FINISH
        );
    }

    @RequiredArgsConstructor
    public static class Stats<T> {
        private final Comparator<? super T> comparator;
        /**
         * Number of items.
         */
        @Getter
        private long count;

        /**
         * Minimum value.
         */
        @Nullable
        @Getter
        private T min;

        /**
         * Maximum value.
         */
        @Nullable
        @Getter
        private T max;

        /**
         * Check if has min value.
         *
         * @return true if has min value, false otherwise.
         */
        public boolean hasMin() {
            // min is null when count is 0 or comparator is Comparator.nullsFirst()
            return count != 0;
        }

        /**
         * Check if has max value.
         *
         * @return true if has max value, false otherwise.
         */
        public boolean hasMax() {
            // max is null when count is 0 or comparator is Comparator.nullsLast()
            return count != 0;
        }

        public void accept(T val) {
            if (count == 0) min = max = val;
            else if (comparator.compare(val, min) < 0) min = val;
            else if (comparator.compare(val, max) > 0) max = val;
            count++;
        }

        public Stats<T> combine(Stats<T> that) {
            if (!this.comparator.equals(that.comparator)) {
                throw new IllegalArgumentException("Cannot combine stats with different comparators");
            }
            if (this.count == 0) return that;
            if (that.count == 0) return this;

            this.count += that.count;

            if (this.comparator.compare(that.min, this.min) < 0) this.min = that.min;
            if (this.comparator.compare(that.max, this.max) > 0) this.max = that.max;

            return that;
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ValueHolder<T, X extends RuntimeException> implements Consumer<T> {
        private final Supplier<X> exceptionSupplier;
        private final AtomicReference<T> reference = new AtomicReference<>();

        @Override
        public void accept(T t) {
            if (!reference.compareAndSet(null, t)) {
                throw exceptionSupplier.get();
            }
        }

        public boolean isPresent() {
            return reference.get() != null;
        }

        public T getValue() {
            return reference.get();
        }
    }
}
