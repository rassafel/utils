package com.rassafel.io.storage.core.impl.keygen

import spock.lang.Specification

class NoOpKeyGeneratorTest extends Specification {
    def gen = NoOpKeyGenerator.getInstance()


    def "CreateKey"() {
        given:

        when:
        def actual = gen.createKey(name)

        then:
        actual == name

        where:
        name << KeyGeneratorTestUtils.names
    }

    def "CreateKey from null"() {
        given:

        when:
        def actual = gen.createKey(null)

        then:
        actual == "null"
    }
}
