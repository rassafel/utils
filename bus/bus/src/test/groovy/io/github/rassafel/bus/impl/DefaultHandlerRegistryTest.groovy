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

package io.github.rassafel.bus.impl

import spock.lang.Specification

import io.github.rassafel.bus.*

class DefaultHandlerRegistryTest extends Specification {
    def registry = new DefaultHandlerRegistry()

    def "get command handler without matches then throws exception"() {
        given:
        CommandHandler<Object, NotMatchedCommand> handler = (command) -> null
        registry.registerCommand(NotMatchedCommand, handler)

        when:
        registry.getCommandHandler(ExactlyMatchedCommand)

        then:
        thrown(CommandNotFoundException)
    }

    def "get command handler exactly matched then returns handler"() {
        given:
        CommandHandler<Object, ExactlyMatchedCommand> handler = (command) -> null
        registry.registerCommand(ExactlyMatchedCommand, handler)

        when:
        def actual = registry.getCommandHandler(ExactlyMatchedCommand)

        then:
        actual.is(handler)
    }

    def "get command handler assigned matched then returns handler"() {
        given:
        CommandHandler<Object, ExactlyMatchedCommand> handler = (command) -> null
        registry.registerCommand(ExactlyMatchedCommand, handler)

        when:
        def actual = registry.getCommandHandler(AssignedMatchedCommand)

        then:
        actual.is(handler)
    }

    def "get command handler deep assigned matched then return handler"() {
        given:
        CommandHandler<Object, ExactlyMatchedCommand> handler = (command) -> null
        registry.registerCommand(ExactlyMatchedCommand, handler)
        CommandHandler<Object, AssignedMatchedCommand> assignedHandler = command -> null
        registry.registerCommand(AssignedMatchedCommand.class, assignedHandler)

        when:
        def actual = registry.getCommandHandler(DeepAssignedMatchedCommand.class)

        then:
        actual.is(assignedHandler)
    }

    def "get command handler parent not matched then throws exception"() {
        given:
        CommandHandler<Object, AssignedMatchedCommand> handler = (command) -> null
        registry.registerCommand(AssignedMatchedCommand, handler)

        when:
        registry.getCommandHandler(ExactlyMatchedCommand)

        then:
        thrown(CommandNotFoundException)
    }

    private interface NotMatchedCommand extends Command<Object> {
    }

    private interface ExactlyMatchedCommand extends Command<Object> {
    }

    private interface AssignedMatchedCommand extends ExactlyMatchedCommand {
    }

    private interface DeepAssignedMatchedCommand extends AssignedMatchedCommand {
    }

    def "get query handler without matches then throws exception"() {
        given:
        QueryHandler<Object, NotMatchedQuery> handler = (command) -> null
        registry.registerQuery(NotMatchedQuery, handler)

        when:
        registry.getQueryHandler(ExactlyMatchedQuery)

        then:
        thrown(QueryNotFoundException)
    }

    def "get query handler exactly matched then returns handler"() {
        given:
        QueryHandler<Object, ExactlyMatchedQuery> handler = (command) -> null
        registry.registerQuery(ExactlyMatchedQuery, handler)

        when:
        def actual = registry.getQueryHandler(ExactlyMatchedQuery)

        then:
        actual.is(handler)
    }

    def "get query handler assigned matched then returns handler"() {
        given:
        QueryHandler<Object, ExactlyMatchedQuery> handler = (command) -> null
        registry.registerQuery(ExactlyMatchedQuery, handler)

        when:
        def actual = registry.getQueryHandler(AssignedMatchedQuery)

        then:
        actual.is(handler)
    }

    def "get query handler deep assigned matched then return handler"() {
        given:
        QueryHandler<Object, ExactlyMatchedQuery> handler = (command) -> null
        registry.registerQuery(ExactlyMatchedQuery, handler)
        QueryHandler<Object, AssignedMatchedQuery> assignedHandler = command -> null
        registry.registerQuery(AssignedMatchedQuery.class, assignedHandler)

        when:
        def actual = registry.getQueryHandler(DeepAssignedMatchedQuery.class)

        then:
        actual.is(assignedHandler)
    }

    def "get query handler parent not matched then throws exception"() {
        given:
        QueryHandler<Object, AssignedMatchedQuery> handler = (command) -> null
        registry.registerQuery(AssignedMatchedQuery, handler)

        when:
        registry.getQueryHandler(ExactlyMatchedQuery)

        then:
        thrown(QueryNotFoundException)
    }

    private interface NotMatchedQuery extends Query<Object> {
    }

    private interface ExactlyMatchedQuery extends Query<Object> {
    }

    private interface AssignedMatchedQuery extends ExactlyMatchedQuery {
    }

    private interface DeepAssignedMatchedQuery extends AssignedMatchedQuery {
    }
}
