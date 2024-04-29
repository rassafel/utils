package com.rassafel.io.storage.core.impl.keygen

import spock.lang.Shared
import spock.lang.Specification

import java.time.*

class CalendarKeyGeneratorTest extends Specification {
    @Shared
    LocalDateTime now = LocalDateTime.of(LocalDate.of(2024, 4, 29), LocalTime.MIDNIGHT)
    @Shared
    Clock clock = Clock.fixed(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    def gen = new CalendarKeyGenerator()

    void setup() {
        gen.clock = clock
    }

    def "CreateKey"() {
        given:

        when:
        def actual = gen.createKey(name)

        then:
        actual == "2024/04/29"

        where:
        name << [*KeyGeneratorTestUtils.names, null]
    }
}
