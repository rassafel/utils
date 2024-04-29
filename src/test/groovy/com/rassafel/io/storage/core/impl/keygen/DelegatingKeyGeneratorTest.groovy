package com.rassafel.io.storage.core.impl.keygen

import spock.lang.Shared
import spock.lang.Specification

class DelegatingKeyGeneratorTest extends Specification {
    def "Empty generators"() {
        given:
        def gen = new DelegatingKeyGenerator()

        when:
        def actual = gen.createKey(name)

        then:
        actual == ""

        where:
        name << [*KeyGeneratorTestUtils.names, null]
    }

    @Shared
    String key = "static"
    @Shared
    StaticKeyGenerator staticKeyGenerator = new StaticKeyGenerator(key)

    def "Single generator"() {
        given:
        def gen = new DelegatingKeyGenerator()
        gen.addGenerator(staticKeyGenerator)

        when:
        def actual = gen.createKey(name)

        then:
        actual == key

        where:
        name << [*KeyGeneratorTestUtils.names, null]
    }

    def "Two generators"() {
        given:
        def gen = new DelegatingKeyGenerator()
        gen.addGenerator(staticKeyGenerator)
        gen.addGenerator(staticKeyGenerator)

        when:
        def actual = gen.createKey(name)

        then:
        actual == "$key/$key"

        where:
        name << [*KeyGeneratorTestUtils.names, null]
    }
}
