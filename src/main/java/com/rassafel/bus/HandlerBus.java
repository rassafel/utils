package com.rassafel.bus;

public interface HandlerBus {
    <R, C extends Command<R>> R executeCommand(C command);

    <R, Q extends Query<R>> R executeQuery(Q command);
}
