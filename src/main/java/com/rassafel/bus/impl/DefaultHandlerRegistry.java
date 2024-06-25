package com.rassafel.bus.impl;

import com.rassafel.bus.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultHandlerRegistry implements HandlerRegistry {
    private final Map<Class<? extends Command>, CommandHandler> commandHandlers;
    private final Map<Class<? extends Command>, CommandHandler> cacheCommandHandlers = new ConcurrentHashMap<>();
    private final Map<Class<? extends Query>, QueryHandler> queryHandlers;
    private final Map<Class<? extends Query>, QueryHandler> cacheQueryHandlers = new ConcurrentHashMap<>();

    public DefaultHandlerRegistry() {
        this(new HashMap<>(), new HashMap<>());
    }

    public DefaultHandlerRegistry(Map<Class<? extends Command>, CommandHandler> commandHandlers,
                                  Map<Class<? extends Query>, QueryHandler> queryHandlers) {
        this.commandHandlers = commandHandlers;
        this.queryHandlers = queryHandlers;
    }

    public <R, C extends Command<R>> void registerCommand(Class<? extends C> type,
                                                          CommandHandler<R, C> handler) {
        commandHandlers.compute(type, (commandType, oldHandler) -> {
            if (oldHandler != null)
                log.debug("Replace type command handler, type: {}; old handler: {}; new handler: {}",
                    type.getName(), oldHandler.getClass().getName(), handler.getClass().getName());
            return handler;
        });
    }

    @Override
    public <R, C extends Command<R>> CommandHandler<R, C> getCommandHandler(Class<C> commandType) {
        var handler = cacheCommandHandlers.computeIfAbsent(commandType, type ->
            commandHandlers.entrySet().stream()
                .filter(e -> e.getKey().isAssignableFrom(type))
                .min(Comparator.comparingInt(e -> inheritanceDeep(e.getKey(), type)))
                .map(Map.Entry::getValue)
                .orElseThrow(() -> {
                    log.warn("CommandHandler not found, command type: {}", commandType.getName());
                    return new CommandNotFoundException();
                }));
        log.debug("Found handler for command, handler: '{}'; command: '{}'",
            handler.getClass().getName(), commandType.getName());
        return handler;
    }

    private int inheritanceDeep(Class<?> superClass, Class<?> type) {
        var classes = ClassUtils.getAllInterfaces(type);
        for (int i = 0; i < classes.size(); i++) {
            var clazz = classes.get(i);
            if (clazz == superClass) {
                return i;
            }
        }
        throw new RuntimeException();
    }

    public <R, Q extends Query<R>> void registerQuery(Class<? extends Q> type,
                                                      QueryHandler<R, Q> handler) {
        queryHandlers.compute(type, (commandType, oldHandler) -> {
            if (oldHandler != null)
                log.debug("Replace type command handler, type: {}; old handler: {}; new handler: {}",
                    type.getName(), oldHandler.getClass().getName(), handler.getClass().getName());
            return handler;
        });
    }

    @Override
    public <R, Q extends Query<R>> QueryHandler<R, Q> getQueryHandler(Class<Q> queryType) {
        var handler = cacheQueryHandlers.computeIfAbsent(queryType, type ->
            queryHandlers.entrySet().stream()
                .filter(e -> e.getKey().isAssignableFrom(type))
                .min(Comparator.comparingInt(e -> inheritanceDeep(e.getKey(), type)))
                .map(Map.Entry::getValue)
                .orElseThrow(() -> {
                    log.warn("QueryHandler not found, command type: {}", queryType.getName());
                    return new QueryNotFoundException();
                }));
        log.debug("Found handler for query, handler: '{}'; query: '{}'",
            handler.getClass().getName(), queryType.getName());
        return handler;
    }
}
