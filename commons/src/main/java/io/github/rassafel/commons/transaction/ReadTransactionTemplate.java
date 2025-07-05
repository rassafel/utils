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

package io.github.rassafel.commons.transaction;


import java.io.Serial;
import java.lang.reflect.UndeclaredThrowableException;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

public class ReadTransactionTemplate extends TransactionTemplate {
    @Serial
    private static final long serialVersionUID = 2989727789797052158L;

    public ReadTransactionTemplate() {
    }

    public ReadTransactionTemplate(PlatformTransactionManager transactionManager) {
        super(transactionManager);
    }

    public ReadTransactionTemplate(PlatformTransactionManager transactionManager, TransactionDefinition transactionDefinition) {
        super(transactionManager, transactionDefinition);
    }

    @Override
    @Nullable
    public <T> T execute(@NonNull TransactionCallback<T> action) throws TransactionException {
        final var transactionManager = getTransactionManager();
        Assert.state(transactionManager != null, "No PlatformTransactionManager set");

        if (transactionManager instanceof CallbackPreferringPlatformTransactionManager cpptm) {
            return cpptm.execute(this, action);
        } else {
            TransactionStatus status = transactionManager.getTransaction(this);
            try {
                return action.doInTransaction(status);
            } catch (RuntimeException | Error ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new UndeclaredThrowableException(ex, "TransactionCallback threw undeclared checked exception");
            } finally {
                // Never rollback
                transactionManager.commit(status);
            }
        }
    }
}
