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

package com.rassafel.publish;


import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.component.ConfigurationVariantDetails;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.internal.JavaPluginHelper;
import org.gradle.api.plugins.jvm.internal.JvmFeatureInternal;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.JavadocMemberLevel;
import org.gradle.external.javadoc.JavadocOutputLevel;
import org.gradle.internal.component.external.model.TestFixturesSupport;
import org.gradle.jvm.component.internal.DefaultJvmSoftwareComponent;
import org.gradle.jvm.tasks.Jar;

public class ModulePublishPlugin implements Plugin<Project> {
    protected Project project;
    protected PublishingExtension publishing;
    protected MavenPublication publication;
    private TaskContainer tasks;

    @Override
    public void apply(Project project) {
        this.project = project;
        tasks = project.getTasks();
        var plugin = project.getPlugins().apply(PublishPlugin.class);
        if (plugin.publishing == null) return;
        publishing = plugin.publishing;
        publication = plugin.publication;
        configureJar();
        configureJavadoc();
        configureSourcesJar();
        configureJavadocJar();
        configurePublication();
        ignoreTestFixtures();
    }

    private TaskProvider<Jar> configureJar() {
        var task = tasks.named("jar", Jar.class);
        task.configure(jar -> {
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
        return task;
    }

    private TaskProvider<Javadoc> configureJavadoc() {
        return tasks.named("javadoc", Javadoc.class, javadoc -> {
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

    private TaskProvider<Jar> configureSourcesJar() {
        return tasks.register("sourcesJar", Jar.class, task -> {
            var java = project.getExtensions().getByType(JavaPluginExtension.class);
            task.dependsOn(tasks.findByName("classes"));
            task.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
            task.getArchiveClassifier().set("sources");
            task.from(java.getSourceSets().getByName("main").getAllSource());
        });
    }

    private TaskProvider<Jar> configureJavadocJar() {
        return tasks.register("javadocJar", Jar.class, task -> {
            task.getArchiveClassifier().set("javadoc");
            task.from(tasks.getByName("javadoc"));
        });
    }

    private void configurePublication() {
        publication.from(project.getComponents().getByName("java"));
        publication.artifact(tasks.getByName("sourcesJar"));
        publication.artifact(tasks.getByName("javadocJar"));
    }

    private void ignoreTestFixtures() {
        var feature = (JvmFeatureInternal) project.getComponents().findByName(TestFixturesSupport.TEST_FIXTURES_FEATURE_NAME);
        if (feature == null) {
            return;
        }
        var component = (DefaultJvmSoftwareComponent) JavaPluginHelper.getJavaComponent(project);
        component.withVariantsFromConfiguration(feature.getApiElementsConfiguration(), ConfigurationVariantDetails::skip);
        component.withVariantsFromConfiguration(feature.getRuntimeElementsConfiguration(), ConfigurationVariantDetails::skip);
    }
}
