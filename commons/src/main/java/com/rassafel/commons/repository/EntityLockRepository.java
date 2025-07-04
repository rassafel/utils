package com.rassafel.commons.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;

public interface EntityLockRepository<T, ID> {
    /**
     * Retrieves an entity by its id and lock if exists.
     *
     * @param id       must not be null.
     * @param lockMode must not be null.
     * @return the entity with the given id or Optional#empty() if none found.
     */
    Optional<T> findByIdWithLock(ID id, LockModeType lockMode);
}
