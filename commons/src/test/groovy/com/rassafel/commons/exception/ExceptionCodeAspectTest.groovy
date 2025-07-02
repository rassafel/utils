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

package com.rassafel.commons.exception

import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.expression.spel.standard.SpelExpressionParser
import spock.lang.Specification

import com.rassafel.commons.spel.SpelResolver

class ExceptionCodeAspectTest extends Specification {
    SystemComponent target = Mock()
    SystemComponent proxyTarget

    void setup() {
        var factory = new AspectJProxyFactory(target)
        var spelResolver = new SpelResolver(new SpelExpressionParser(), new DefaultParameterNameDiscoverer())
        factory.addAspect(new ExceptionCodeAspect(spelResolver))
        proxyTarget = factory.getProxy()
    }

    def applicationException(Throwable ex) {
        ex.suppressed.find(ApplicationException::isInstance) as ApplicationException
    }

    def noApplicationException(Throwable ex) {
        !ex.getSuppressed().any(ApplicationException::isInstance)
    }

    def "no throw exception"() {
        when:
        proxyTarget.single()

        then:
        noExceptionThrown()
    }

    def "single"() {
        when:
        proxyTarget.single()

        then:
        def ex = thrown(RuntimeException)
        verifyAll(applicationException(ex)) {
            code == "1"
            type == ExceptionCode.DEFAULT_TYPE
        }
        1 * target.single() >> { throw new IllegalArgumentException() }
    }

    def "no matched single"() {
        when:
        proxyTarget.single()

        then:
        def ex = thrown(RuntimeException)
        noApplicationException(ex)
        1 * target.single() >> { throw new RuntimeException() }
    }

    def "inheritance parent"() {
        when:
        proxyTarget.inheritance()

        then:
        def ex = thrown(RuntimeException)
        verifyAll(applicationException(ex)) {
            code == "1"
            type == ExceptionCode.DEFAULT_TYPE
        }
        1 * target.inheritance() >> { throw new RuntimeException() }
    }

    def "inheritance child"() {
        when:
        proxyTarget.inheritance()

        then:
        def ex = thrown(IllegalStateException)
        verifyAll(applicationException(ex)) {
            code == "1"
            type == ExceptionCode.DEFAULT_TYPE
        }
        1 * target.inheritance() >> { throw new IllegalStateException() }
    }

    def "inheritance declared child"() {
        when:
        proxyTarget.inheritance()

        then:
        def ex = thrown(IllegalArgumentException)
        verifyAll(applicationException(ex)) {
            code == "2"
            type == "INPUT"
        }
        1 * target.inheritance() >> { throw new IllegalArgumentException() }
    }

    def "detail"() {
        given:
        var id = "faca1c7d-8d54-402e-837d-a492202f4a8c"
        var complex = new SystemComponent.ComplexObject(id)

        when:
        proxyTarget.detail(id, complex)

        then:
        def ex = thrown(RuntimeException)
        verifyAll(applicationException(ex)) {
            details["simple"] == "simple"
            details["simpleArgument"] == id
            details["complexArgument"] == id
            details["argumentByIndex"] == id
            details["complexArgumentByIndex"] == id
            details["parameterByIndex"] == id
            details["complexParameterByIndex"] == id
            details["className"] == SystemComponent.name
            details["targetClassName"] == target.class.name
            details["methodName"] == "detail"
            details["key"] == "key"
            details["expression"] == "expression $id"
            details["argument"] == id
            details["argumentName"] == "id"
            details["arguments"] == "$id,SystemComponent.ComplexObject(id=$id)"
            details["argumentsNames"] == "id,complex"
        }
        1 * target.detail(id, complex) >> { throw new RuntimeException() }
    }

    def "without annotation"() {
        when:
        proxyTarget.withoutAnnotation()

        then:
        def ex = thrown(RuntimeException)
        noApplicationException(ex)
        1 * target.withoutAnnotation() >> { throw new RuntimeException() }
    }
}
