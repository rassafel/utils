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
            .should().beAnnotatedWith("org.springframework.lang.NonNullApi")
            .allowEmptyShould(true);
    }

    static ArchRule nullableAnnotation() {
        return ArchRuleDefinition.noClasses()
            .should().dependOnClassesThat()
            .haveFullyQualifiedName("javax.annotation.Nullable")
            .orShould().dependOnClassesThat()
            .haveFullyQualifiedName("jakarta.annotation.Nullable")
            .orShould().dependOnClassesThat()
            .haveFullyQualifiedName("org.jetbrains.annotations.Nullable")
            .orShould().dependOnClassesThat()
            .haveFullyQualifiedName("io.micrometer.common.lang.Nullable")
            .orShould().dependOnClassesThat()
            .haveFullyQualifiedName("org.springframework.lang.Nullable");
    }
}
