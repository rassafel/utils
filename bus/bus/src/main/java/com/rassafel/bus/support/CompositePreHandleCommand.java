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

package com.rassafel.bus.support;


import com.rassafel.bus.Command;
import com.rassafel.bus.CommandHandler;
import com.rassafel.bus.PreHandleCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CompositePreHandleCommand implements PreHandleCommand {
    private final List<PreHandleCommand> handlers = new ArrayList<>();

    public CompositePreHandleCommand() {
    }

    public CompositePreHandleCommand(Collection<PreHandleCommand> handlers) {
        setHandlers(handlers);
    }

    public void setHandlers(Collection<PreHandleCommand> handlers) {
        this.handlers.clear();
        if (handlers != null) {
            this.handlers.addAll(handlers);
        }
    }

    public void addHandler(PreHandleCommand handler) {
        handlers.add(handler);
    }

    @Override
    public void accept(CommandHandler<?, ?> handler, Command<?> command) {
        for (var preHandler : handlers) {
            preHandler.accept(handler, command);
        }
    }
}
