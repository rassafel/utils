package com.rassafel.commons.builder;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
abstract class TestBody {
    private final String body;

    protected TestBody(BuilderImpl builder) {
        this.body = builder.body;
    }

    public abstract Builder toBuilder();

    public interface Builder {
        Builder body(String test);

        TestBody build();
    }

    @Getter
    protected static abstract class BuilderImpl implements Builder {
        private String body;

        protected BuilderImpl() {
        }

        protected BuilderImpl(TestBody testBody) {
            body(testBody.body);
        }

        @Override
        public Builder body(String body) {
            this.body = body;
            return this;
        }
    }
}
