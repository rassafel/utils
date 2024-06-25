package com.rassafel.bus.impl;

import com.rassafel.bus.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultHandlerBus implements HandlerBus {
    private final HandlerRegistry registry;

    @Override
    public <R, C extends Command<R>> R executeCommand(C command) {
        var handler = (CommandHandler<R, C>) registry.getCommandHandler(command.getClass());
        return handler.handle(command);
    }

    @Override
    public <R, Q extends Query<R>> R executeQuery(Q query) {
        var handler = (QueryHandler<R, Q>) registry.getQueryHandler(query.getClass());
        return handler.handle(query);
    }
}
