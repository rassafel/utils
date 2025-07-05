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

package io.github.rassafel.blobstorage

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

import spock.lang.Shared
import spock.lang.Specification
import spock.util.time.MutableClock

import io.github.rassafel.blobstorage.core.BlobStorage
import io.github.rassafel.blobstorage.core.NotFoundBlobException
import io.github.rassafel.blobstorage.core.impl.keygen.StaticKeyGenerator
import io.github.rassafel.blobstorage.core.query.impl.DefaultStoreBlobRequest
import io.github.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesRequest

import static io.github.rassafel.blobstorage.BlobStorageTestUtils.*

abstract class BlobStorageSpecification extends Specification {

    @Shared
    LocalDateTime now = LocalDateTime.of(LocalDate.of(2024, 4, 30), LocalTime.MIDNIGHT)
    @Shared
    LocalDateTime tickedNow = now
    @Shared
    MutableClock clock = new MutableClock(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    @Shared
    def staticKey = "static"
    @Shared
    def keyGen = new StaticKeyGenerator(staticKey)
    def body = "Test body"
    def name = "test.txt"
    def expectedRef = "/${staticKey}.txt"

    abstract BlobStorage storage();

    def "before store check exists"() {
        when:
        def deleteRes = storage().deleteByRef(expectedRef)

        then:
        !deleteRes
        existsCheck(false, expectedRef)
        checkBeforeStoreCheckExists()
    }

    def checkBeforeStoreCheckExists() {
        true
    }

    def "before store update"() {
        given:
        def request = DefaultUpdateAttributesRequest.builder()
                .attribute("X-Replace-Meta", "Value4")
                .attribute("X-New-Meta", "Value5")
                .removeAttribute("X-Delete-Meta")
                .build()

        when:
        def response = storage().updateByRef(expectedRef, request)
                .getStoredObject()

        then:
        thrown(NotFoundBlobException)
        checkBeforeStoreUpdate()
    }

    def checkBeforeStoreUpdate() {
        true
    }

    def "before store and update check exists"() {
        when:
        def deleteRes = storage().deleteByRef(expectedRef)

        then:
        !deleteRes
        existsCheck(false, expectedRef)
        checkBeforeStoreAndUpdateCheckExists()
    }

    def checkBeforeStoreAndUpdateCheckExists() {
        true
    }

    def "store"() {
        given:
        def request = DefaultStoreBlobRequest.builder()
                .originalName(name)
                .attribute("X-Meta", "Value1")
                .attribute("X-Replace-Meta", "Value2")
                .attribute("X-Delete-Meta", "Value3")
                .build()

        when:
        def response = storage().store(toInputStream(body), request)

        then:
        response != null
        verifyAll(response.getStoredObject()) {
            getAttribute("X-Meta") == "Value1"
            getAttribute("X-Replace-Meta") == "Value2"
            getAttribute("X-Delete-Meta") == "Value3"
            getContentType() == "text/plain"
            getOriginalName() == name
            blobToString(it) == body
            getUploadedAt() == now
            getLastModifiedAt() == now
            getSize() == getBytesSize(body)
            getStoredRef() == expectedRef
        }
        checkStore()
    }

    def checkStore() {
        true
    }

    def "after store check exists"() {
        expect:
        existsCheck(true, expectedRef)
        checkAfterStoreCheckExists()
    }

    def checkAfterStoreCheckExists() {
        true
    }

    def "update after store"() {
        given:
        tickedNow = now.plusSeconds(10)
        clock.setInstant(tickedNow.toInstant(ZoneOffset.UTC))
        def request = DefaultUpdateAttributesRequest.builder()
                .attribute("X-Replace-Meta", "Value4")
                .attribute("X-New-Meta", "Value5")
                .removeAttribute("X-Delete-Meta")
                .build()

        when:
        def response = storage().updateByRef(expectedRef, request)

        then:
        response != null
        verifyAll(response.getStoredObject()) {
            getAttribute("X-Delete-Meta") == null
            getAttribute("X-Meta") == "Value1"
            getAttribute("X-Replace-Meta") == "Value4"
            getAttribute("X-New-Meta") == "Value5"
            getContentType() == "text/plain"
            getOriginalName() == name
            blobToString(it) == body
            getUploadedAt() == now
            getLastModifiedAt() == tickedNow
            getSize() == getBytesSize(body)
            getStoredRef() == expectedRef
        }
        checkUpdateAfterStore()
    }

    def checkUpdateAfterStore() {
        true
    }

    def "after update check exists"() {
        expect:
        existsCheck(true, expectedRef)
    }

    def checkAfterUpdateCheckExists() {
        true
    }

    def "delete"() {
        when:
        def result = storage().deleteByRef(expectedRef)

        then:
        result
        checkDelete()
    }

    def checkDelete() {
        true
    }

    def "after delete check exists"() {
        expect:
        existsCheck(false, expectedRef)
        checkAfterDeleteCheckExists()
    }

    def checkAfterDeleteCheckExists() {
        true
    }

    def "after delete again delete"() {
        when:
        def result = storage().deleteByRef(expectedRef)

        then:
        !result
        checkAfterDeleteAgainDelete()
    }

    def checkAfterDeleteAgainDelete() {
        true
    }

    def "after delete update"() {
        given:
        def request = DefaultUpdateAttributesRequest.builder()
                .attribute("X-Fail-Edit", "Value6")
                .build()

        when:
        def response = storage().updateByRef(expectedRef, request)

        then:
        thrown(NotFoundBlobException)
        checkAfterDeleteUpdate()
    }

    def checkAfterDeleteUpdate() {
        true
    }

    def existsCheck(boolean expected, String ref) {
        def obj = storage().getByRef(ref)
        def objOpt = storage().findByRef(ref)
        def exists = storage().existsByRef(ref)

        (obj != null) == expected && exists == expected && objOpt.isPresent() == expected
    }
}
