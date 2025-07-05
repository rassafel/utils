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


import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import lombok.*;
import org.springframework.lang.Nullable;

/**
 * A simple immutable pair.
 *
 * @param <L> the type of the left element
 * @param <R> the type of the right element
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
@EqualsAndHashCode
public final class Pair<L, R> implements Serializable, Map.Entry<L, R> {
    @Serial
    private static final long serialVersionUID = -2972065193810266654L;
    @Nullable
    private final L left;

    @Nullable
    private final R right;

    /**
     * Create a new pair.
     *
     * @param left  the left element
     * @param right the right element
     * @param <L>   the type of the left element
     * @param <R>   the type of the right element
     * @return the new pair
     */
    public static <L, R> Pair<L, R> of(@Nullable L left, @Nullable R right) {
        return new Pair<>(left, right);
    }

    /**
     * Create a new pair with only the left element. Right element is null.
     *
     * @param left the left element
     * @param <L>  the type of the left element
     * @param <R>  the type of the right element
     * @return the new pair
     */
    public static <L, R> Pair<L, R> left(@Nullable L left) {
        return of(left, null);
    }

    /**
     * Create a new pair with only the right element. Left element is null.
     *
     * @param right the right element
     * @param <L>   the type of the right element
     * @param <R>   the type of the left element
     * @return the new pair
     */
    public static <L, R> Pair<L, R> right(@Nullable R right) {
        return of(null, right);
    }

    /**
     * Create a comparator that compares pairs by their left element.
     * Left elements are compared using {@link Comparable#compareTo(Object)}.
     *
     * @param <L> the type of the left element
     * @param <R> the type of the right element
     * @return a comparator that compares pairs by their left element
     */
    public static <L extends Comparable<? super L>, R> Comparator<Pair<L, R>> comparingByLeft() {
        return (pair1, pair2) -> pair1.left.compareTo(pair2.left);
    }

    /**
     * Create a comparator that compares pairs by their right element.
     * Right elements are compared using {@link Comparable#compareTo(Object)}.
     *
     * @param <L> the type of the left element
     * @param <R> the type of the right element
     * @return a comparator that compares pairs by their right element
     */
    public static <L, R extends Comparable<? super R>> Comparator<Pair<L, R>> comparingByRight() {
        return (pair1, pair2) -> pair1.right.compareTo(pair2.right);
    }

    /**
     * Create a comparator that compares pairs by their left element.
     *
     * @param comparator the comparator to use for comparison of left elements
     * @param <L>        the type of the left element
     * @param <R>        the type of the right element
     * @return a comparator that compares pairs by their left element
     */
    public static <L, R> Comparator<Pair<L, R>> comparingByLeft(@NonNull Comparator<? super L> comparator) {
        return (pair1, pair2) -> comparator.compare(pair1.left, pair2.left);
    }

    /**
     * Create a comparator that compares pairs by their right element.
     *
     * @param comparator the comparator to use for comparison of right elements
     * @param <L>        the type of the left element
     * @param <R>        the type of the right element
     * @return a comparator that compares pairs by their right element
     */
    public static <L, R> Comparator<Pair<L, R>> comparingByRight(@NonNull Comparator<? super R> comparator) {
        return (pair1, pair2) -> comparator.compare(pair1.right, pair2.right);
    }

    /**
     * Create a list containing the left and right elements of the pair
     *
     * @param pair the pair to convert
     * @param <T>  the type of the elements in the list
     * @return a list containing the left and right elements of the pair
     */
    public static <T> List<T> toList(@NonNull Pair<? extends T, ? extends T> pair) {
        return Arrays.asList(pair.left, pair.right);
    }

    /**
     * Get the left element.
     *
     * @return the left element or null if it does not exist
     */
    @Nullable
    @Override
    public L getKey() {
        return left;
    }

    /**
     * Get the right element.
     *
     * @return the right element or null if it does not exist
     */
    @Nullable
    @Override
    public R getValue() {
        return right;
    }

    /**
     * Set the value of this entry. Throws an UnsupportedOperationException
     *
     * @param value the value to set
     * @return the old value
     * @throws UnsupportedOperationException always thrown
     */
    @Override
    public R setValue(R value) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * Get the left element.
     *
     * @return the left element or null if it does not exist
     */
    @Nullable
    public L getFirst() {
        return left;
    }

    /**
     * Get the right element.
     *
     * @return the right element or null if it does not exist
     */
    @Nullable
    public R getSecond() {
        return right;
    }

    /**
     * Check if the left element exists.
     *
     * @return true if the left element exists, false otherwise
     */
    public boolean hasLeft() {
        return left != null;
    }

    /**
     * Check if the right element exists.
     *
     * @return true if the right element exists, false otherwise
     */
    public boolean hasRight() {
        return right != null;
    }

    /**
     * Create a new pair with the same right element with a new left element.
     *
     * @param left the new left element
     * @param <V>  the type of the new left element
     * @return a new pair with the same right element and the new left element
     */
    public <V> Pair<V, R> withLeft(@Nullable V left) {
        return new Pair<>(left, right);
    }

    /**
     * Create a new pair with the same left element with a new right element.
     *
     * @param right the new right element
     * @param <V>   the type of the new right element
     * @return a new pair with the same left element and the new right element
     */
    public <V> Pair<L, V> withRight(@Nullable V right) {
        return new Pair<>(left, right);
    }

    /**
     * Map the left element using a function.
     *
     * @param function the function to map the left element
     * @param <V>      the type of the mapped left element
     * @return a new pair with the mapped left element and the original right element
     */
    public <V> Pair<V, R> mapLeft(@NonNull Function<? super L, ? extends V> function) {
        return withLeft(function.apply(left));
    }

    /**
     * Map the right element using a function.
     *
     * @param function the function to map the right element
     * @param <V>      the type of the mapped right element
     * @return a new pair with the original left element and the mapped right element
     */
    public <V> Pair<L, V> mapRight(@NonNull Function<? super R, ? extends V> function) {
        return withRight(function.apply(right));
    }

    /**
     * Swap the left and right elements.
     *
     * @return a new pair with the swapped elements
     */
    public Pair<R, L> swap() {
        return new Pair<>(right, left);
    }

    /**
     * Convert the pair to an array.
     *
     * @return an array containing the left and right elements in that order
     */
    public Object[] toArray() {
        return new Object[]{left, right};
    }

    /**
     * Convert the pair to a list.
     * Use static {@link Pair#toList(Pair)} method instead of this one.
     *
     * @return a list containing the left and right elements in that order
     */
    public List<Object> toList() {
        return Arrays.asList(left, right);
    }
}
