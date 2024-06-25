package com.rassafel.bus;

public class QueryNotFoundException extends HandlerException {
    public QueryNotFoundException() {
    }

    public QueryNotFoundException(String message) {
        super(message);
    }

    public QueryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryNotFoundException(Throwable cause) {
        super(cause);
    }
}
