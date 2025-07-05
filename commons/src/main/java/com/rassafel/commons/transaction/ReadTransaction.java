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


import java.lang.annotation.*;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only transaction annotation.
 * Without rollbacks on exceptions.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Transactional(readOnly = true, noRollbackFor = Exception.class)
@Documented
public @interface ReadTransaction {
    /**
     * @see Transactional#value()
     */
    @AliasFor(annotation = Transactional.class, attribute = "value")
    String value() default "";

    /**
     * @see Transactional#transactionManager()
     */
    @AliasFor(annotation = Transactional.class, attribute = "transactionManager")
    String transactionManager() default "";

    /**
     * @see Transactional#label()
     */
    @AliasFor(annotation = Transactional.class, attribute = "label")
    String[] label() default {};

    /**
     * @see Transactional#propagation()
     */
    @AliasFor(annotation = Transactional.class, attribute = "propagation")
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * @see Transactional#isolation()
     */
    @AliasFor(annotation = Transactional.class, attribute = "isolation")
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * @see Transactional#timeout()
     */
    @AliasFor(annotation = Transactional.class, attribute = "timeout")
    int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

    /**
     * @see Transactional#timeoutString()
     */
    @AliasFor(annotation = Transactional.class, attribute = "timeoutString")
    String timeoutString() default "";
}
