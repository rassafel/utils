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

package com.rassafel.bus.impl;

import com.rassafel.bus.*;

public class DefaultHandlerBus implements HandlerBus {
    private final HandlerRegistry registry;
    private final PreHandleCommand commandPreHandler;
    private final PreHandleQuery queryPreHandler;

    public DefaultHandlerBus(HandlerRegistry registry) {
        this(registry,
            query -> {
            },
            command -> {
            });
    }

    public DefaultHandlerBus(HandlerRegistry registry, PreHandleCommand commandPreHandler, PreHandleQuery queryPreHandler) {
        this.registry = registry;
        this.commandPreHandler = commandPreHandler;
        this.queryPreHandler = queryPreHandler;
    }

    @Override
    public <R, C extends Command<R>> R executeCommand(C command) {
        var handler = (CommandHandler<R, C>) registry.getCommandHandler(command.getClass());
        commandPreHandler.accept(command);
        return handler.handle(command);
    }

    @Override
    public <R, Q extends Query<R>> R executeQuery(Q query) {
        var handler = (QueryHandler<R, Q>) registry.getQueryHandler(query.getClass());
        queryPreHandler.accept(query);
        return handler.handle(query);
    }
}
