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

package io.github.rassafel.bus.config;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.GenericTypeResolver;

import io.github.rassafel.bus.*;
import io.github.rassafel.bus.impl.DefaultHandlerBus;
import io.github.rassafel.bus.impl.DefaultHandlerRegistry;
import io.github.rassafel.bus.support.CompositePreHandleCommand;
import io.github.rassafel.bus.support.CompositePreHandleQuery;

@Configuration
public class HandlerBusConfiguration {
    private final List<CommandHandler> commandHandlers = new ArrayList<>();
    private final List<QueryHandler> queryHandlers = new ArrayList<>();
    private final List<PreHandleCommand> preCommandHandlers = new ArrayList<>();
    private final List<PreHandleQuery> preQueryHandlers = new ArrayList<>();

    @Autowired(required = false)
    public void setCommandHandlers(@Nullable List<CommandHandler> commandHandlers) {
        if (commandHandlers != null) {
            this.commandHandlers.addAll(commandHandlers);
        }
    }

    @Autowired(required = false)
    public void setQueryHandlers(@Nullable List<QueryHandler> queryHandlers) {
        if (queryHandlers != null) {
            this.queryHandlers.addAll(queryHandlers);
        }
    }

    @Autowired(required = false)
    public void setPreCommandHandlers(@Nullable List<PreHandleCommand> preCommandHandlers) {
        if (preCommandHandlers != null) {
            this.preCommandHandlers.addAll(preCommandHandlers);
        }
    }

    public void setPreQueryHandlers(List<PreHandleQuery> preQueryHandlers) {
        if (preQueryHandlers != null) {
            this.preQueryHandlers.addAll(preQueryHandlers);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public HandlerRegistry handlerRegistry() {
        var registry = new DefaultHandlerRegistry();
        commandHandlers.forEach(handler -> {
            var commandType = getHandlerCommandType(handler);
            registry.registerCommand(commandType, handler);
        });
        queryHandlers.forEach(handler -> {
            var queryType = getHandlerQueryType(handler);
            registry.registerQuery(queryType, handler);
        });
        return registry;
    }

    private Class<? extends Command> getHandlerCommandType(CommandHandler handler) {
        var handlerTypes = GenericTypeResolver.resolveTypeArguments(handler.getClass(), CommandHandler.class);
        return (Class<? extends Command>) handlerTypes[1];
    }

    private Class<? extends Query> getHandlerQueryType(QueryHandler handler) {
        var handlerTypes = GenericTypeResolver.resolveTypeArguments(handler.getClass(), QueryHandler.class);
        return (Class<? extends Query>) handlerTypes[1];
    }

    @Bean
    @ConditionalOnMissingBean
    public PreHandleCommand preHandleCommand() {
        return new CompositePreHandleCommand(preCommandHandlers);
    }

    @Bean
    @ConditionalOnMissingBean
    public PreHandleQuery preHandleQuery() {
        return new CompositePreHandleQuery(preQueryHandlers);
    }

    @Bean
    @ConditionalOnMissingBean
    public HandlerBus handlerBus(
            HandlerRegistry registry, PreHandleCommand preHandleCommand, PreHandleQuery preHandleQuery) {
        return new DefaultHandlerBus(registry, preHandleCommand, preHandleQuery);
    }
}
