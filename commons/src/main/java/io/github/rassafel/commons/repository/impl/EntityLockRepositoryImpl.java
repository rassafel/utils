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

package io.github.rassafel.commons.repository.impl;

import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.core.RepositoryMethodContext;
import org.springframework.data.repository.core.support.RepositoryMetadataAccess;
import org.springframework.transaction.annotation.Transactional;

import io.github.rassafel.commons.repository.EntityLockRepository;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntityLockRepositoryImpl<T, ID>
        implements EntityLockRepository<T, ID>, RepositoryMetadataAccess {
    private final EntityManager entityManager;

    @Override
    public Optional<T> findByIdWithLock(@NonNull ID id, @NonNull LockModeType lockMode) {
        var domainClass = getDomainClass();
        var entity = entityManager.find(domainClass, id, lockMode, null);
        return Optional.ofNullable(entity);
    }

    @SuppressWarnings("unchecked")
    private Class<T> getDomainClass() {
        return (Class<T>) RepositoryMethodContext.getContext().getMetadata().getDomainType();
    }
}
