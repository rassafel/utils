package com.rassafel.io.storage.core.impl.keygen

import spock.lang.Specification


class IncrementKeyGeneratorTest extends Specification {
    def gen = new IncrementKeyGenerator(1, 1)

    def "CreateKey"() {
        given:

        when:
        def actual = gen.createKey(name)

        then:
        actual == "1"

        where:
        name << [*KeyGeneratorTestUtils.names, null]
    }

    def "CreateKey twice"() {
        given:

        when:
        def actual = gen.createKey(name)

        then:
        actual == "1"

        when:
        actual = gen.createKey(name)

        then:
        actual == "2"

        where:
        name << [*KeyGeneratorTestUtils.names, null]
    }
}
