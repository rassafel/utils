package com.rassafel.commons.builder;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuilderTest {
    @Test
    public void create() {
        val actual = TestChildBody.builder()
            .body("Test 1")
            .version(1)
            .build();
        assertThat(actual)
            .isNotNull()
            .matches(e -> e.getVersion() == 1)
            .matches(e -> "Test 1".equals(e.getBody()));
    }

    @Test
    public void mutateBuilder() {
        val actual = TestChildBody.builder().version(2)
            .applyMutation(b -> b.body("Test 2"))
            .build();
        assertThat(actual)
            .isNotNull()
            .matches(e -> e.getVersion() == 2)
            .matches(e -> "Test 2".equals(e.getBody()));
    }

    @Test
    public void mutateBuiltObject() {
        val initial = TestChildBody.builder().version(3)
            .applyMutation(b -> b.body("Test 3"))
            .build();
        assertThat(initial)
            .isNotNull()
            .matches(e -> e.getVersion() == 3)
            .matches(e -> "Test 3".equals(e.getBody()));
        val actual = initial.toBuilder().version(4)
            .applyMutation(b -> b.body("Test 4"))
            .build();
        assertThat(actual)
            .isNotNull()
            .matches(e -> e.getVersion() == 4)
            .matches(e -> "Test 4".equals(e.getBody()));
        assertThat(initial)
            .isNotNull()
            .matches(e -> e.getVersion() == 3)
            .matches(e -> "Test 3".equals(e.getBody()));
    }

    @Test
    public void copy() {
        val initial = TestChildBody.builder().version(5)
            .applyMutation(b -> b.body("Test 5"))
            .build();
        val actual = initial.copy(e -> {
        });
        assertThat(actual)
            .isNotSameAs(initial)
            .isNotNull()
            .matches(e -> e.getVersion() == 5)
            .matches(e -> "Test 5".equals(e.getBody()));
    }

    @Test
    public void copyBuilder() {
        val initial = TestChildBody.builder().version(6)
            .applyMutation(b -> b.body("Test 6"));
        val item1 = initial.copy().build();
        val item2 = initial.body("Test 7")
            .version(7).build();

        assertThat(item1)
            .isNotSameAs(item2)
            .isNotNull()
            .matches(e -> e.getVersion() == 6)
            .matches(e -> "Test 6".equals(e.getBody()));

        assertThat(item2)
            .isNotNull()
            .matches(e -> e.getVersion() == 7)
            .matches(e -> "Test 7".equals(e.getBody()));
    }
}
