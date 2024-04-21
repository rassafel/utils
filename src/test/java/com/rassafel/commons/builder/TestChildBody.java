package com.rassafel.commons.builder;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
class TestChildBody extends TestBody implements ToCopyableBuilder<TestChildBody.Builder, TestChildBody> {
    private final int version;

    public TestChildBody(BuilderImpl builder) {
        super(builder);
        this.version = builder.version;
    }

    @Override
    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    public interface Builder extends TestBody.Builder, CopyableBuilder<Builder, TestChildBody> {
        Builder version(int version);

        @Override
        Builder body(String test);
    }

    @Getter
    protected static class BuilderImpl extends TestBody.BuilderImpl implements Builder {
        private int version = 0;

        private BuilderImpl() {
            super();
        }

        private BuilderImpl(TestChildBody body) {
            super(body);
            version(body.version);
        }

        @Override
        public Builder version(int version) {
            this.version = version;
            return this;
        }

        @Override
        public Builder body(String body) {
            super.body(body);
            return this;
        }

        @Override
        public TestChildBody build() {
            return new TestChildBody(this);
        }
    }
}
