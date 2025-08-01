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


import io.freefair.gradle.plugins.lombok.LombokPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import io.github.rassafel.architecture.ArchitecturePlugin;

public class BuildConventionsPlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) {
        var plugins = project.getPlugins();
        plugins.apply(ArchitecturePlugin.class);
        plugins.apply(LombokPlugin.class);
        new JavaConventions().apply(project);
        new TestConventions().apply(project);
        new FormatConventions().apply(project);
    }
}
