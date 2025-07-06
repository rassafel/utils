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

package io.github.rassafel.architecture;


import java.util.Set;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

public class ArchitectureRules {
    static ArchRule noClassesShouldCallStringToLowerCaseWithoutLocale() {
        return ArchRuleDefinition.noClasses()
            .should()
            .callMethod(String.class, "toLowerCase")
            .because("String.toLowerCase(Locale.ROOT) should be used instead");
    }

    static ArchRule noClassesShouldCallStringToUpperCaseWithoutLocale() {
        return ArchRuleDefinition.noClasses()
            .should()
            .callMethod(String.class, "toUpperCase")
            .because("String.toUpperCase(Locale.ROOT) should be used instead");
    }

    static ArchRule packageInfoShouldBeNullMarked() {
        return ArchRuleDefinition.classes()
            .that().haveSimpleName("package-info")
            .should().beAnnotatedWith("org.jspecify.annotations.NullMarked")
            .allowEmptyShould(true);
    }

    static ArchRule nullableAnnotation() {
        return ArchRuleDefinition.noClasses()
            .should().dependOnClassesThat(haveNameEndingWithAndNotName(
                Set.of(".Nullable", ".Null"),
                Set.of("jakarta.validation.constraints.Null", "org.jspecify.annotations.Nullable")
            ));
    }

    static ArchRule nonNullAnnotation() {
        return ArchRuleDefinition.noClasses()
            .should().dependOnClassesThat(haveNameEndingWithAndNotName(
                Set.of(".NonNull", ".NotNull"),
                Set.of("lombok.NonNull", "jakarta.validation.constraints.NotNull", "org.jspecify.annotations.NonNull")
            ));
    }

    private static DescribedPredicate<HasName> haveNameEndingWithAndNotName(Set<String> suffixes, Set<String> names) {
        if (suffixes.isEmpty()) {
            return DescribedPredicate.alwaysFalse();
        }
        var builder = new StringBuilder("have name with ");
        if (suffixes.size() == 1) builder
            .append("suffix ")
            .append(suffixes.iterator().next());
        else builder
            .append("one of the following suffixes ")
            .append("['")
            .append(String.join(", ", suffixes))
            .append("']");
        if (names.size() == 1) builder
            .append(" and do not have name ")
            .append(names.iterator().next());
        else if (names.size() > 1) builder
            .append(" and do not have one of the following names ")
            .append("['")
            .append(String.join(", ", names))
            .append("']");

        return DescribedPredicate.describe(builder.toString(), hasName -> {
            var name = hasName.getName();
            for (var suffix : suffixes) {
                if (name.endsWith(suffix)) {
                    return !names.contains(name);
                }
            }
            return false;
        });
    }
}
