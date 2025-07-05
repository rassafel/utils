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
class TestChildBody extends TestBody<TestChildBody, TestChildBody.Builder> {
    private final int version;

    public TestChildBody(BuilderImpl builder) {
        super(builder);
        this.version = builder.version;
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public interface Builder extends TestBody.Builder<TestChildBody, Builder> {
        Builder version(int version);
    }

    protected static class BuilderImpl extends AbstractBuilder<TestChildBody, Builder> implements Builder {
        protected int version = 0;

        protected BuilderImpl() {
            super();
        }

        protected BuilderImpl(TestChildBody body) {
            super(body);
            version(body.version);
        }

        @Override
        public Builder version(int version) {
            this.version = version;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public TestChildBody build() {
            return new TestChildBody(this);
        }
    }
}
