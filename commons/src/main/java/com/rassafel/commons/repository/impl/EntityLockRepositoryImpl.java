package com.rassafel.commons.repository.impl;

import java.util.Optional;

import com.rassafel.commons.repository.EntityLockRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.core.RepositoryMethodContext;
import org.springframework.data.repository.core.support.RepositoryMetadataAccess;
import org.springframework.transaction.annotation.Transactional;

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
