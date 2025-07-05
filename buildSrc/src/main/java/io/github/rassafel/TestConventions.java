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


import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.tasks.JacocoReport;

public class TestConventions {
    protected Project project;

    public final void apply(Project project) {
        this.project = project;
        project.getPlugins().withType(JavaBasePlugin.class, (java) -> configureTestConventions());
    }

    private void configureTestConventions() {
        project.getTasks().withType(Test.class, test -> {
            configureTests(test);
            configureJaCoCo(test);
        });
    }

    private void configureTests(Test test) {
        var existingOptions = test.getOptions();
        test.useJUnitPlatform(options -> {
            if (existingOptions instanceof JUnitPlatformOptions junitPlatformOptions) {
                options.copyFrom(junitPlatformOptions);
            }
        });
        test.include("**/*Tests.class", "**/*Test.class");
        test.setSystemProperties(Map.of(
            "java.awt.headless", "true",
            "junit.platform.discovery.issue.severity.critical", "INFO"
        ));
        if (project.hasProperty("testGroups")) {
            test.systemProperty("testGroups", project.getProperties().get("testGroups"));
        }
        test.jvmArgs(
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED",
            "-Xshare:off"
        );
    }

    private void configureJaCoCo(Test test) {
        if (!project.getPlugins().hasPlugin(JacocoPlugin.class)) return;
        project.getTasks().withType(JacocoReport.class, task -> {
            task.dependsOn(test);

            task.reports(reports -> {
                reports.getHtml().getRequired().set(true);
                reports.getXml().getRequired().set(isCi());
            });
        });
    }

    private boolean isCi() {
        return Boolean.parseBoolean(System.getenv("CI"));
    }
}
