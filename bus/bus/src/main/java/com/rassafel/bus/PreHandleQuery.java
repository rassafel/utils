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

package com.rassafel.bus;

/**
 * Pre-handle query interface. This interface is used to pre-process queries before they are handled by a query.
 */
public interface PreHandleQuery {
    /**
     * Accepts the query and handles it using the provided handler.
     *
     * @param handler the handler to handle the query
     * @param query   the query to be handled
     */
    void accept(QueryHandler<?, ?> handler, Query<?> query);
}
