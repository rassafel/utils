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

package io.github.rassafel.publish;


import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.component.ConfigurationVariantDetails;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.JavadocMemberLevel;
import org.gradle.external.javadoc.JavadocOutputLevel;
import org.gradle.jvm.component.internal.DefaultJvmSoftwareComponent;
import org.gradle.jvm.tasks.Jar;

public class ModulePublishPlugin implements Plugin<Project> {
    public static final String JAVADOC_ARCHIVE = "javadoc";
    public static final String SOURCES_ARCHIVE = "sources";
    public static final String JAVADOC_JAR_TASK = "javadocJar";
    public static final String SOURCES_JAR_TASK = "sourcesJar";
    protected Project project;
    protected MavenPublication publication;
    private TaskContainer tasks;

    @Override
    public void apply(Project project) {
        this.project = project;
        var plugin = project.getPlugins().apply(PublishPlugin.class);
        if (plugin.publication == null) return;

        tasks = project.getTasks();
        publication = plugin.publication;

        configureJar();
        configureJavadoc();
        configurePublication();
        ignoreTestFixtures();
    }

    private void configureJar() {
        jarTask().configure(jar -> {
            jar.manifest(manifest -> {
                var attributes = new LinkedHashMap<String, Object>();
                attributes.put("Implementation-Title", project.getName());
                attributes.put("Implementation-Version", project.getVersion());
                attributes.put("Automatic-Module-Name", project.getName().replace("-", "."));
                attributes.put("Created-By", String.format("%s %s", System.getProperty("java.version"), System.getProperty("java.specification.vendor")));
                manifest.attributes(attributes);
            });

            jar.from(project.getRootDir() + "/docs/dist", copy -> {
                copy.include("license.txt");
                copy.include("notice.txt");

                copy.into("META-INF");
                copy.expand(Map.of(
                    "version", project.getVersion(),
                    "copyright", DateTimeFormatter.ofPattern("yyyy").format(LocalDate.now())
                ));
            });
        });
    }

    private void configureJavadoc() {
        javadocTask().configure(javadoc -> {
            javadoc.setDescription("Generates project-level javadoc for use in -javadoc jar");
            javadoc.setFailOnError(true);
            javadoc.options(minOptions -> {
                minOptions.encoding(StandardCharsets.UTF_8.displayName());
                minOptions.setOutputLevel(JavadocOutputLevel.QUIET);
                minOptions.setMemberLevel(JavadocMemberLevel.PROTECTED);

                var options = minOptions.header(project.getName())
                    .use(true)
                    .author(true);
                options.addBooleanOption("Xdoclint:syntax,-reference", true);
                options.addBooleanOption("Werror", false);
            });
            javadoc.getLogging().captureStandardError(LogLevel.INFO);
            javadoc.getLogging().captureStandardOutput(LogLevel.INFO);
        });
    }

    private void configurePublication() {
        publication.from(javaComponents());
        publication.artifact(sourcesJarTask());
        publication.artifact(javadocJarTask());
    }

    private TaskProvider<Jar> sourcesJarTask() {
        if (tasks.findByName(SOURCES_JAR_TASK) != null) return tasks.named(SOURCES_JAR_TASK, Jar.class);
        return tasks.register(SOURCES_JAR_TASK, Jar.class, task -> {
            var java = project.getExtensions().getByType(JavaPluginExtension.class);
            task.dependsOn(tasks.named("classes"));
            task.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
            task.getArchiveClassifier().set(SOURCES_ARCHIVE);
            task.from(java.getSourceSets().getByName("main").getAllSource());
        });
    }

    private TaskProvider<Jar> javadocJarTask() {
        if (tasks.findByName(JAVADOC_JAR_TASK) != null) return tasks.named(JAVADOC_JAR_TASK, Jar.class);
        return tasks.register(JAVADOC_JAR_TASK, Jar.class, task -> {
            task.getArchiveClassifier().set(JAVADOC_ARCHIVE);
            task.from(javadocTask());
        });
    }

    private TaskProvider<Jar> jarTask() {
        return tasks.named("jar", Jar.class);
    }

    private TaskProvider<Javadoc> javadocTask() {
        return tasks.named("javadoc", Javadoc.class);
    }

    private void ignoreTestFixtures() {
        var component = javaComponents();
        var configurations = project.getConfigurations();
        Stream.of("testFixturesApiElements", "testFixturesRuntimeElements")
            .map(configurations::findByName)
            .filter(Objects::nonNull)
            .forEach(config ->
                component.withVariantsFromConfiguration(config, ConfigurationVariantDetails::skip)
            );
    }

    private DefaultJvmSoftwareComponent javaComponents() {
        return (DefaultJvmSoftwareComponent) project.getComponents().getByName("java");
    }
}
