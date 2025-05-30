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

package com.rassafel.commons.web

import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification
import spock.lang.Subject


class RequestIdRequestFilterTest extends Specification {
    @Subject
    def filter = new RequestIdRequestFilter(() -> "1234567890")

    def "should set request id"() {
        given:
        def request = new MockHttpServletRequest()
        def response = new MockHttpServletResponse()
        def chain = new MockFilterChain()

        when:
        filter.doFilter(request, response, chain)

        then:
        request.getAttribute(RequestIdRequestFilter.REQUEST_ID_ATTRIBUTE_NAME) == "1234567890"
        response.getHeader(RequestIdRequestFilter.DEFAULT_REQUEST_ID_HEADER) == "1234567890"
    }
}
