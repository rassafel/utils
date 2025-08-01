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

package io.github.rassafel.commons.builder;

import lombok.Getter;

@Getter
abstract class TestBody<O extends TestBody<O, B>, B extends TestBody.Builder<O, B>> implements ToCopyableBuilder<O, B> {
    protected final String body;

    protected TestBody(AbstractBuilder<O, B> builder) {
        this.body = builder.body;
    }

    public interface Builder<O extends TestBody<O, B>, B extends Builder<O, B>> extends CopyableBuilder<O, B> {
        B body(String test);
    }

    protected abstract static class AbstractBuilder<O extends TestBody<O, B>, B extends Builder<O, B>>
            implements Builder<O, B> {
        protected String body;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(O testBody) {
            body(testBody.body);
        }

        @Override
        public B body(String body) {
            this.body = body;
            return self();
        }

        protected abstract B self();
    }
}
