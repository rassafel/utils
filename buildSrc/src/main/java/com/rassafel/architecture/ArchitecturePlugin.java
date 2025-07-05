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

package com.rassafel.architecture;


import java.util.ArrayList;
import java.util.Locale;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

public class ArchitecturePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().withType(JavaPlugin.class, (javaPlugin) -> registerTasks(project));
    }

    private void registerTasks(Project project) {
        var javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        var architectureChecks = new ArrayList<>();
        for (var sourceSet : javaPluginExtension.getSourceSets()) {
            if (sourceSet.getName().toLowerCase(Locale.ROOT).contains(SourceSet.TEST_SOURCE_SET_NAME)) {
                // skip test source sets.
                continue;
            }
            var checkArchitecture = project.getTasks()
                .register(taskName(sourceSet), ArchitectureCheck.class,
                    (task) -> {
                        task.setClasses(sourceSet.getOutput().getClassesDirs());
                        task.getResourcesDirectory().set(sourceSet.getOutput().getResourcesDir());
                        task.dependsOn(sourceSet.getProcessResourcesTaskName());
                        task.setDescription("Checks the architecture of the classes of the " + sourceSet.getName()
                            + " source set.");
                        task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
                    });
            architectureChecks.add(checkArchitecture);
        }
        if (!architectureChecks.isEmpty()) {
            var checkTask = project.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME);
            checkTask.configure((check) -> check.dependsOn(architectureChecks));
        }
    }

    private static String taskName(SourceSet sourceSet) {
        return "checkArchitecture"
            + sourceSet.getName().substring(0, 1).toUpperCase()
            + sourceSet.getName().substring(1);
    }
}
