package com.rassafel.commons.version;

/**
 * Interface for objects that have a version.
 *
 * @param <V> version type
 */
public interface Versionable<V> {
    /**
     * Get the version of this object.
     *
     * @return the version
     */
    V getVersion();
}
