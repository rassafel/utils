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
 * A bus that handles commands and queries.
 */
public interface HandlerBus {
    /**
     * Executes a command and returns the result of the execution.
     *
     * @param command the command to be executed.
     * @param <R>     the type of the result.
     * @param <C>     the type of the command.
     * @return the result of the execution.
     */
    <R, C extends Command<R>> R executeCommand(C command);

    /**
     * Executes a query and returns the result of the execution.
     *
     * @param query the query to be executed.
     * @param <R>   the type of the result.
     * @param <Q>   the type of the query.
     * @return the result of the execution.
     */
    <R, Q extends Query<R>> R executeQuery(Q query);
}
