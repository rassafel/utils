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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

public class JavaConventions {
    private static final List<String> COMPILER_ARGS;
    private static final List<String> TEST_COMPILER_ARGS;

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
    protected JavaLanguageVersion releaseVersion;
    protected JavaLanguageVersion languageVersion;

    protected JavaPlugin javaPlugin;
    protected JavaPluginExtension javaExtension;

    public final void apply(Project project) {
        this.project = project;
        this.log = project.getLogger();
        var libs = project.getExtensions().getByType(VersionCatalogsExtension.class);
        releaseVersion = JavaLanguageVersion.of(17);
        languageVersion = libs.named("libs").findVersion("java-library")
            .map(Object::toString)
            .map(JavaLanguageVersion::of)
            .orElse(releaseVersion);
        javaPlugin = project.getPlugins().getPlugin(JavaPlugin.class);
        javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        applyToolchainConventions();
        applyJavaCompileConventions();
    }

    private void applyToolchainConventions() {
        javaExtension.toolchain(toolchain -> {
            toolchain.getLanguageVersion().set(languageVersion);
        });
    }

    private void applyJavaCompileConventions() {
        project.afterEvaluate(p -> {
            p.getTasks().withType(JavaCompile.class)
                .matching(this::isMainCompile)
                .forEach(compileTask -> {
                    configureCompileOptions(compileTask.getOptions(), COMPILER_ARGS);
                    setJavaRelease(compileTask);
                });
            p.getTasks().withType(JavaCompile.class)
                .matching(this::isTestCompile)
                .forEach(compileTask -> {
                    configureCompileOptions(compileTask.getOptions(), TEST_COMPILER_ARGS);
                    setJavaRelease(compileTask);
                });

        });
    }

    private boolean isMainCompile(JavaCompile compileTask) {
        return compileTask.getName().startsWith(JavaPlugin.COMPILE_JAVA_TASK_NAME);
    }

    private boolean isTestCompile(JavaCompile compileTask) {
        var name = compileTask.getName();
        return name.startsWith(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME)
            || name.equals("compileTestFixturesJava");
    }

    private void configureCompileOptions(CompileOptions options, Collection<String> args) {
        var compilerArgs = options.getCompilerArgs();
        for (var arg : args) {
            if (!compilerArgs.contains(arg)) {
                compilerArgs.add(arg);
            }
        }
        if (options.getEncoding() != null) {
            return;
        }
        options.setEncoding(StandardCharsets.UTF_8.displayName());
    }

    private void setJavaRelease(JavaCompile task) {
        var defaultVersion = releaseVersion.asInt();
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
}
