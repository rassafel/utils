package com.rassafel.bus;

public interface HandlerRegistry {
    <R, C extends Command<R>> CommandHandler<R, C> getCommandHandler(Class<C> commandType);

    <R, Q extends Query<R>> QueryHandler<R, Q> getQueryHandler(Class<Q> queryType);
}
