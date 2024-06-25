package com.rassafel.bus.config;

import com.rassafel.bus.*;
import com.rassafel.bus.impl.DefaultHandlerBus;
import com.rassafel.bus.impl.DefaultHandlerRegistry;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.GenericTypeResolver;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class HandlerBusConfiguration {
    private List<CommandHandler> commandHandlers;
    private List<QueryHandler> queryHandlers;

    @Autowired(required = false)
    public void setCommandHandlers(@Nullable List<CommandHandler> commandHandlers) {
        this.commandHandlers = commandHandlers == null ? new ArrayList<>() : commandHandlers;
    }

    @Autowired(required = false)
    public void setQueryHandlers(@Nullable List<QueryHandler> queryHandlers) {
        this.queryHandlers = queryHandlers == null ? new ArrayList<>() : queryHandlers;
    }

    @ConditionalOnMissingBean
    @Bean
    public HandlerRegistry handlerRegistry() {
        val registry = new DefaultHandlerRegistry();
        commandHandlers.forEach(handler -> {
            val commandType = getHandlerCommandType(handler);
            registry.registerCommand(commandType, handler);
        });
        queryHandlers.forEach(handler -> {
            val queryType = getHandlerQueryType(handler);
            registry.registerQuery(queryType, handler);
        });
        return registry;
    }

    private Class<? extends Command> getHandlerCommandType(CommandHandler handler) {
        val handlerTypes = GenericTypeResolver.resolveTypeArguments(handler.getClass(), CommandHandler.class);
        return (Class<? extends Command>) handlerTypes[1];
    }

    private Class<? extends Query> getHandlerQueryType(QueryHandler handler) {
        val handlerTypes = GenericTypeResolver.resolveTypeArguments(handler.getClass(), QueryHandler.class);
        return (Class<? extends Query>) handlerTypes[1];
    }

    @ConditionalOnMissingBean
    @Bean
    public HandlerBus handlerBus(HandlerRegistry registry) {
        return new DefaultHandlerBus(registry);
    }
}
