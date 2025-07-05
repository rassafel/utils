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

package io.github.rassafel.commons.repository;

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
