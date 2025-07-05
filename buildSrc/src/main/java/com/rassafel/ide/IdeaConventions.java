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

package com.rassafel.ide;


import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.plugins.ide.idea.model.IdeaModule;
import org.gradle.plugins.ide.idea.model.IdeaProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.gradle.ext.CopyrightConfiguration;
import org.jetbrains.gradle.ext.EncodingConfiguration;
import org.jetbrains.gradle.ext.IdeaExtPlugin;
import org.jetbrains.gradle.ext.ProjectSettings;

public class IdeaConventions {
    private Project project;
    private IdeaModel model;
    private PluginContainer plugins;

    void apply(@NotNull Project project) {
        this.project = project;
        plugins = project.getPlugins();
        var ideaPlugin = plugins.apply(IdeaPlugin.class);
        plugins.apply(IdeaExtPlugin.class);
        model = ideaPlugin.getModel();
        if (model == null) return;
        configureModule(model.getModule());
        configureProject(model.getProject());
    }

    private void configureModule(@Nullable IdeaModule module) {
        if (module == null) return;
        module.setDownloadJavadoc(true);
        module.setDownloadSources(true);
    }

    private void configureProject(@Nullable IdeaProject project) {
        if (project == null) return;
        var settings = findExtension(project, ProjectSettings.class);
        if (settings == null) return;
        configureCopyright(findExtension(settings, CopyrightConfiguration.class));
        configureEncoding(findExtension(settings, EncodingConfiguration.class));
    }

    private void configureCopyright(@Nullable CopyrightConfiguration configuration) {
        if (configuration == null) return;
        var profile = configuration.getProfiles().create("DefaultCopyright");
        profile.setNotice(APACHE_NOTICE);
        configuration.setUseDefault(profile.getName());
    }

    private void configureEncoding(@Nullable EncodingConfiguration configuration) {
        if (configuration == null) return;
        configuration.setEncoding(StandardCharsets.UTF_8.displayName());
        configuration.setBomPolicy(EncodingConfiguration.BomPolicy.WITH_NO_BOM);
        var properties = configuration.getProperties();
        properties.setEncoding(StandardCharsets.UTF_8.displayName());
        properties.setTransparentNativeToAsciiConversion(false);
    }

    @Nullable
    protected <E> E findExtension(Object container, Class<E> type) {
        if (container instanceof ExtensionAware aware) {
            return aware.getExtensions().findByType(type);
        }
        return null;
    }

    private static final String APACHE_NOTICE = """
            Copyright 2024-$today.year the original author or authors.

            Licensed under the Apache License, Version 2.0 (the "License");
            you may not use this file except in compliance with the License.
            You may obtain a copy of the License at

                 https://www.apache.org/licenses/LICENSE-2.0

            Unless required by applicable law or agreed to in writing, software
            distributed under the License is distributed on an "AS IS" BASIS,
            WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
            See the License for the specific language governing permissions and
            limitations under the License.""";
}
