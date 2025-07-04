package com.rassafel.commons.version;


import java.util.function.Supplier;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

@Slf4j
@UtilityClass
public class VersionUtils {
    /**
     * Checks whether the given versionable has a version that matches the expected one.
     *
     * @param versionable the versionable to check
     * @param expected    the expected version
     * @param <V>         the type of the version
     * @return true if the versions match, false otherwise
     */
    public static <V> boolean matches(Versionable<V> versionable, @Nullable V expected) {
        var actual = versionable.getVersion();
        if (expected == null) return false;
        return expected.equals(actual);
    }

    /**
     * Checks whether the given version matches the given one.
     *
     * @param actual   the actual version
     * @param expected the expected version
     * @param <V>      the type of the version
     * @return true if the versions match, false otherwise
     */
    public static <V> boolean matches(V actual, @Nullable V expected) {
        if (expected == null) return false;
        return expected.equals(actual);
    }

    /**
     * Checks whether the given versionable has a version that matches the expected one.
     *
     * @param versionable the versionable to check
     * @param expected    the expected version
     * @param <V>         the type of the version
     * @throws IllegalArgumentException if the versions do not match
     */
    public static <V> void checkVersion(Versionable<V> versionable, @Nullable V expected) throws IllegalArgumentException {
        checkVersion(versionable, expected,
                () -> new IllegalArgumentException("Expected " + expected + ", but got " + versionable.getVersion()));
    }

    /**
     * Checks whether the given versionable has a version that matches the expected one.
     *
     * @param versionable       the versionable to check
     * @param expected          the expected version
     * @param exceptionSupplier the supplier of an exception to be thrown if the versions do not match
     * @param <V>               the type of the version
     * @param <X>               the type of the exception to be thrown
     * @throws X the type of the exception to be thrown
     */
    public static <V, X extends Throwable> void checkVersion(Versionable<V> versionable, @Nullable V expected, Supplier<X> exceptionSupplier) throws X {
        var actual = versionable.getVersion();
        if (expected == null || !expected.equals(actual)) {
            logMismatch(versionable, expected);
            throw exceptionSupplier.get();
        }
        logCorrect(versionable);
    }

    private static <V> void logMismatch(Versionable<V> versionable, @Nullable V expected) {
        if (!log.isDebugEnabled()) return;
        var actual = versionable.getVersion();
        var name = versionable.getClass().getSimpleName();
        if (versionable instanceof Persistable<?> persistable) {
            var id = persistable.getId();
            log.debug("{} version mismatch, entity ID: {}; expected: {}; actual: {}", name, id, expected, actual);
            return;
        }
        log.debug("{} version mismatch, expected: {}; actual: {}", name, expected, actual);
    }

    private static <V> void logCorrect(Versionable<V> versionable) {
        if (!log.isTraceEnabled()) return;
        var actual = versionable.getVersion();
        var name = versionable.getClass().getSimpleName();
        if (versionable instanceof Persistable<?> persistable) {
            var id = persistable.getId();
            log.trace("{} version correct, entity ID: {}; version: {}", name, id, actual);
            return;
        }
        log.trace("{} version correct, version: {}", name, actual);
    }
}
