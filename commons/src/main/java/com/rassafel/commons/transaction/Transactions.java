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

package com.rassafel.commons.transaction;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class Transactions implements TransactionOperations {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final Lock readLock = lock.readLock();
    private final Map<String, TransactionOperations> operationsCache = new ConcurrentHashMap<>(2);
    @Nullable
    private PlatformTransactionManager transactionManager;

    @NonNull
    private final TransactionDefinition defaultDefinition;

    public Transactions() {
        this(TransactionDefinition.withDefaults());
    }

    public Transactions(@Nullable PlatformTransactionManager transactionManager) {
        this(transactionManager, TransactionDefinition.withDefaults());
    }

    public void setTransactionManager(@Nullable PlatformTransactionManager transactionManager) {
        if (this.transactionManager == transactionManager) return;
        writeLock.lock();
        this.transactionManager = transactionManager;
        operationsCache.clear();
        writeLock.unlock();
    }

    @Override
    @Nullable
    public <T> T execute(@NonNull TransactionCallback<T> callback) {
        return getOperations(defaultDefinition).execute(callback);
    }

    public TransactionOperations withDefenition(@NonNull TransactionDefinition defenition) {
        return getOperations(defenition);
    }

    protected TransactionOperations getOperations(TransactionDefinition defenition) {
        var identifier = getDefinitionIdentifier(defenition);
        readLock.lock();
        var operations = transactionManager == null ? operationsCache.computeIfAbsent("default",
                ignored -> TransactionOperations.withoutTransaction()) : operationsCache.computeIfAbsent(identifier,
                ignored -> {
                    var transactionTemplate = defenition.isReadOnly() ?
                            new ReadTransactionTemplate(transactionManager, defenition) :
                            new TransactionTemplate(transactionManager, defenition);
                    transactionTemplate.afterPropertiesSet();
                    return transactionTemplate;
                });
        readLock.unlock();
        return operations;
    }

    protected static String getDefinitionIdentifier(TransactionDefinition defenition) {
        StringBuilder result = new StringBuilder();
        result.append(getPropagationBehaviorName(defenition.getPropagationBehavior()));
        result.append(',');
        result.append(getIsolationLevelName(defenition.getIsolationLevel()));
        if (defenition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
            result.append(',');
            result.append("timeout_").append(defenition.getTimeout());
        }
        if (defenition.isReadOnly()) {
            result.append(',');
            result.append("readOnly");
        }
        return result.toString();
    }

    private static String getPropagationBehaviorName(int propagationBehavior) {
        return switch (propagationBehavior) {
            case TransactionDefinition.PROPAGATION_REQUIRED -> "PROPAGATION_REQUIRED";
            case TransactionDefinition.PROPAGATION_SUPPORTS -> "PROPAGATION_SUPPORTS";
            case TransactionDefinition.PROPAGATION_MANDATORY -> "PROPAGATION_MANDATORY";
            case TransactionDefinition.PROPAGATION_REQUIRES_NEW -> "PROPAGATION_REQUIRES_NEW";
            case TransactionDefinition.PROPAGATION_NOT_SUPPORTED -> "PROPAGATION_NOT_SUPPORTED";
            case TransactionDefinition.PROPAGATION_NEVER -> "PROPAGATION_NEVER";
            case TransactionDefinition.PROPAGATION_NESTED -> "PROPAGATION_NESTED";
            default -> throw new IllegalArgumentException("Unsupported propagation behavior: " + propagationBehavior);
        };
    }

    private static String getIsolationLevelName(int isolationLevel) {
        return switch (isolationLevel) {
            case TransactionDefinition.ISOLATION_DEFAULT -> "ISOLATION_DEFAULT";
            case TransactionDefinition.ISOLATION_READ_UNCOMMITTED -> "ISOLATION_READ_UNCOMMITTED";
            case TransactionDefinition.ISOLATION_READ_COMMITTED -> "ISOLATION_READ_COMMITTED";
            case TransactionDefinition.ISOLATION_REPEATABLE_READ -> "ISOLATION_REPEATABLE_READ";
            case TransactionDefinition.ISOLATION_SERIALIZABLE -> "ISOLATION_SERIALIZABLE";
            default -> throw new IllegalArgumentException("Unsupported isolation level: " + isolationLevel);
        };
    }
}
