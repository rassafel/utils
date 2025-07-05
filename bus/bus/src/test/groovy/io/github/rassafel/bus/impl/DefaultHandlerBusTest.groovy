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

class DefaultHandlerBusTest extends Specification {
    def registry = Mock(HandlerRegistry)
    def commandPreHandler = Mock(PreHandleCommand)
    def queryPreHandler = Mock(PreHandleQuery)
    def bus = new DefaultHandlerBus(registry, commandPreHandler, queryPreHandler)

    def commandHandler = Mock(CommandHandler)
    def queryHandler = Mock(QueryHandler)

    def "execute command when command throws exception then throws exception"() {
        given:
        def command = new Command() {}

        when:
        bus.executeCommand(command)

        then:
        thrown(RuntimeException)
        1 * registry.getCommandHandler(_) >> commandHandler
        1 * commandPreHandler.accept(_, _)
        1 * commandHandler.handle(_) >> { throw new RuntimeException() }
    }

    def "execute command when registry throws exception then throws exception"() {
        given:
        def command = new Command() {}

        when:
        bus.executeCommand(command)

        then:
        thrown(RuntimeException)
        1 * registry.getCommandHandler(_) >> { throw new RuntimeException() }
    }

    def "execute command success"() {
        given:
        def command = new Command() {}
        def result = new Object()

        when:
        def actual = bus.executeCommand(command)

        then:
        1 * registry.getCommandHandler(_) >> commandHandler
        1 * commandPreHandler.accept(_, _)
        1 * commandHandler.handle(_) >> result

        actual.is(result)
    }


    def "execute query when command throws exception then throws exception"() {
        given:
        def query = new Query() {}

        when:
        bus.executeQuery(query)

        then:
        thrown(RuntimeException)
        1 * registry.getQueryHandler(_) >> queryHandler
        1 * queryPreHandler.accept(_, _)
        1 * queryHandler.handle(_) >> { throw new RuntimeException() }
    }

    def "execute query when registry throws exception then throws exception"() {
        given:
        def query = new Query() {}

        when:
        bus.executeQuery(query)

        then:
        thrown(RuntimeException)
        1 * registry.getQueryHandler(_) >> { throw new RuntimeException() }
    }

    def "execute query success"() {
        given:
        def query = new Query() {}
        def result = new Object()

        when:
        def actual = bus.executeQuery(query)

        then:
        1 * registry.getQueryHandler(_) >> queryHandler
        1 * queryPreHandler.accept(_, _)
        1 * queryHandler.handle(_) >> result

        actual.is(result)
    }
}
