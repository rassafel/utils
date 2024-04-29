package com.rassafel.io.storage.core.impl.keygen

import spock.lang.Specification

class UuidKeyGeneratorTest extends Specification {
    def gen = new UuidKeyGenerator()

    def "CreateKey"() {
        given:

        when:
        def actual = gen.createKey(name)

        then:
        UUID.fromString(actual)

        where:
        name << [*KeyGeneratorTestUtils.names, null]
    }
}
