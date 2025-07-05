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

package io.github.rassafel;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.diffplug.gradle.spotless.*;
import org.gradle.api.Project;
import org.gradle.api.plugins.GroovyBasePlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin;

public class FormatConventions {
    private static final List<String> SPOTLESS_MUTATORS;
    private static final List<String> SPOTLESS_IMPORT_ORDER;
    protected Project project;

    public final void apply(Project project) {
        this.project = project;
        configureSpotless();
    }

    private void configureSpotless() {
        project.getPlugins().apply(SpotlessPlugin.class);
        var spotless = project.getExtensions().findByType(SpotlessExtension.class);
        if (spotless == null) return;
        spotless.encoding(StandardCharsets.UTF_8);
        if (project.getPlugins().hasPlugin(JavaBasePlugin.class)) {
            spotless.java(this::configureJavaSpotless);
        }
        if (project.getPlugins().hasPlugin(GroovyBasePlugin.class)) {
            spotless.groovy(groovy -> {
                configureGroovy(groovy);
                groovy.excludeJava();
            });
        }
        if (project.getPlugins().hasPlugin(KotlinBasePlugin.class)) {
            spotless.kotlin(this::configureKotlin);
        }
        spotless.groovyGradle(this::configureGroovy);
        spotless.kotlinGradle(this::configureKotlin);
    }

    private void configureJavaSpotless(@Nullable JavaExtension java) {
        if (java == null) return;
        configureDefaultSpotless(java);
        java.cleanthat().version("2.20")
                .sourceCompatibility("17")
                .addMutators(SPOTLESS_MUTATORS);
        java.removeUnusedImports();
        java.importOrder(SPOTLESS_IMPORT_ORDER.toArray(String[]::new));
    }

    private void configureGroovy(@Nullable BaseGroovyExtension groovy) {
        if (groovy == null) return;
        configureDefaultSpotless(groovy);
        groovy.importOrder(SPOTLESS_IMPORT_ORDER.toArray(String[]::new));
    }

    private void configureKotlin(@Nullable BaseKotlinExtension kotlin) {
        if (kotlin == null) return;
        configureDefaultSpotless(kotlin);
    }

    private void configureDefaultSpotless(@Nullable FormatExtension extension) {
        if (extension == null) return;
        extension.toggleOffOn("@formatter:off", "@formatter:on");
        extension.leadingTabsToSpaces();
        extension.trimTrailingWhitespace();
        extension.endWithNewline();
    }

    static {
        var mutators = new ArrayList<String>();

        mutators.add("SafeAndConsensual");
        mutators.add("SafeButNotConsensual");
        mutators.add("ArithmethicAssignment");
        mutators.add("ArraysDotStream");
        mutators.add("AvoidMultipleUnaryOperators");
        mutators.add("CollectionIndexOfToContains");
        mutators.add("LiteralsFirstInComparisons");
        mutators.add("ModifierOrder");
        mutators.add("OptionalNotEmpty");
        mutators.add("RemoveExplicitCallToSuper");
        mutators.add("SimplifyBooleanExpression");
        mutators.add("SimplifyBooleanInitialization");
        mutators.add("SimplifyStartsWith");
        mutators.add("StringIndexOfToContains");
        mutators.add("StringToString");
        mutators.add("UnnecessaryBoxing");
        mutators.add("UnnecessaryModifier");
        mutators.add("UseCollectionIsEmpty");
        mutators.add("UseStringIsEmpty");
        mutators.add("UseUnderscoresInNumericLiterals");
        mutators.add("AvoidUncheckedExceptionsInSignatures");
        mutators.add("CollectionToOptional");
        mutators.add("EnumsWithoutEquals");
        mutators.add("LambdaIsMethodReference");
        mutators.add("LambdaReturnsSingleStatement");
        mutators.add("LocalVariableTypeInference");
        mutators.add("PrimitiveWrapperInstantiation");
        mutators.add("RemoveAllToClearCollection");
        mutators.add("UnnecessaryFullyQualifiedName");
        mutators.add("UnnecessaryImport");
        mutators.add("UnnecessaryLambdaEnclosingParameters");
        mutators.add("UnnecessarySemicolon");
        mutators.add("UseDiamondOperatorJdk8");
        mutators.add("UsePredefinedStandardCharset");

        SPOTLESS_MUTATORS = Collections.unmodifiableList(mutators);


        var importOrder = new ArrayList<String>();
        var imports = new ArrayList<String>();

        imports.add("java");
        imports.add("javax");
        imports.add("jakarta");
        imports.add("");
        imports.add("io.github.rassafel");

        // imports
        importOrder.addAll(imports);
        // static imports
        importOrder.addAll(imports.stream().map(i -> "\\#" + i).toList());

        SPOTLESS_IMPORT_ORDER = Collections.unmodifiableList(importOrder);
    }
}
