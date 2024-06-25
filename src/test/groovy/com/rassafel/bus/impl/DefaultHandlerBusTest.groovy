package com.rassafel.bus.impl

import com.rassafel.bus.*
import spock.lang.Specification

class DefaultHandlerBusTest extends Specification {
    def registry = Mock(HandlerRegistry)
    def bus = new DefaultHandlerBus(registry)

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
        1 * queryHandler.handle(_) >> result

        actual.is(result)
    }
}
