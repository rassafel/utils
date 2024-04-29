package com.rassafel.io.storage.core.impl.keygen

import spock.lang.Specification

class StaticKeyGeneratorTest extends Specification {
    def key = "static"
    def gen = new StaticKeyGenerator(key)

    def "CreateKey"() {
        given:

        when:
        def actual = gen.createKey(name)

        then:
        actual == key

        where:
        name << [*KeyGeneratorTestUtils.names, null]
    }
}
