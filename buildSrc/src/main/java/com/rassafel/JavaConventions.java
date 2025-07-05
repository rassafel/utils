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

package com.rassafel;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

public class JavaConventions {
    private static final List<String> COMPILER_ARGS;
    private static final List<String> TEST_COMPILER_ARGS;
    private static final JavaLanguageVersion DEFAULT_LANGUAGE_VERSION = JavaLanguageVersion.of(17);
    private static final JavaLanguageVersion DEFAULT_RELEASE_VERSION = JavaLanguageVersion.of(17);

    static {
        var commonCompilerArgs = List.of(
                "-Xlint:cast", "-Xlint:dep-ann", "-Xlint:divzero", "-Xlint:empty",
                "-Xlint:fallthrough", "-Xlint:-options", "-Xlint:overrides",
                "-Xlint:serial", "-Xlint:static",
                "-parameters"
        );

        var compilerArgs = new ArrayList<String>();
        compilerArgs.addAll(commonCompilerArgs);
        compilerArgs.add("-Werror");
        COMPILER_ARGS = Collections.unmodifiableList(compilerArgs);

        var testCompilerArgs = new ArrayList<String>();
        testCompilerArgs.addAll(commonCompilerArgs);
        TEST_COMPILER_ARGS = Collections.unmodifiableList(testCompilerArgs);
    }

    protected Project project;
    protected Logger log;

    public void apply() {
        project.getPlugins().withType(JavaBasePlugin.class, javaPlugin -> {
            applyToolchainConventions();
            applyJavaCompileConventions();
        });
    }

    private void applyToolchainConventions() {
        var extension = project.getExtensions().findByType(JavaPluginExtension.class);
        if (extension != null) extension.toolchain(toolchain -> {
            toolchain.getLanguageVersion().set(DEFAULT_LANGUAGE_VERSION);
        });
    }

    private void applyJavaCompileConventions() {
        project.afterEvaluate(p -> {
            p.getTasks().withType(JavaCompile.class)
                .matching(compileTask -> compileTask.getName().startsWith(JavaPlugin.COMPILE_JAVA_TASK_NAME))
                .forEach(compileTask -> {
                    compileTask.getOptions().setCompilerArgs(COMPILER_ARGS);
                    compileTask.getOptions().setEncoding(StandardCharsets.UTF_8.displayName());
                    setJavaRelease(compileTask);
                });
            p.getTasks().withType(JavaCompile.class)
                .matching(compileTask -> compileTask.getName().startsWith(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME)
                    || compileTask.getName().equals("compileTestFixturesJava"))
                .forEach(compileTask -> {
                    compileTask.getOptions().setCompilerArgs(TEST_COMPILER_ARGS);
                    compileTask.getOptions().setEncoding(StandardCharsets.UTF_8.displayName());
                    setJavaRelease(compileTask);
                });

        });
    }

    private void setJavaRelease(JavaCompile task) {
        var defaultVersion = DEFAULT_RELEASE_VERSION.asInt();
        var releaseVersion = defaultVersion;
        var compilerVersion = task.getJavaCompiler().get().getMetadata().getLanguageVersion().asInt();
        for (var version = defaultVersion; version <= compilerVersion; version++) {
            if (task.getName().contains("Java" + version)) {
                releaseVersion = version;
                break;
            }
        }
        task.getOptions().getRelease().set(releaseVersion);
    }

    public final void apply(Project project) {
        this.project = project;
        this.log = project.getLogger();
        apply();
    }
}
