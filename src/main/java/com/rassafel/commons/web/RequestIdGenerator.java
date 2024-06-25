package com.rassafel.commons.web;

import java.util.function.Supplier;

@FunctionalInterface
public interface RequestIdGenerator extends Supplier<String> {
}
