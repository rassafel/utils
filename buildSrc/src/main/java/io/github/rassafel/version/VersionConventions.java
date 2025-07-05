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

package io.github.rassafel.version;


import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import io.github.rassafel.util.StringUtils;

public class VersionConventions {
    private static final Collection<String> BRANCH_NAMES;

    static {
        BRANCH_NAMES = Set.of(
                "master",
                "main",
                "develop"
        );
    }

    private Project project;
    private Logger log;

    void apply(Project project) {
        this.project = project;
        this.log = project.getLogger();
        if (needReplaceVersion(project.getVersion())) {
            final Object unspecifiedVersion = resolveVersion();
            if (unspecifiedVersion == null) {
                log.warn("Failed to infer the project version. Keeping: {}", project.getVersion());
            } else {
                log.lifecycle("Using unspecified version: {}", unspecifiedVersion);
                project.setVersion(unspecifiedVersion);
                project.allprojects(p -> p.setVersion(project.getVersion()));
            }
        }
    }

    protected boolean needReplaceVersion(Object versionObj) {
        var version = versionObj.toString();
        return isVersionUnspecified(version) || isBranchVersion(version);
    }

    protected boolean isVersionUnspecified(String version) {
        return "unspecified".equals(version);
    }

    protected boolean isBranchVersion(String version) {
        if (!version.endsWith("-SNAPSHOT")) return false;
        version = StringUtils.substringBefore(version, "-SNAPSHOT");
        return BRANCH_NAMES.contains(version);
    }

    @Nullable
    protected Object resolveVersion() {
        return project.findProperty("io.github.rassafel.version.unspecified");
    }
}
