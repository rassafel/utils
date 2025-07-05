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


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.VariantVersionMappingStrategy;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.jetbrains.annotations.NotNull;

public class PublishPlugin implements Plugin<Project> {
    protected Project project;
    protected PublishingExtension publishing;
    protected MavenPublication publication;

    @Override
    public void apply(@NotNull Project project) {
        this.project = project;
        if (isPublishDisabled()) return;

        this.project.getPlugins().apply(MavenPublishPlugin.class);

        publishing = this.project.getExtensions().getByType(PublishingExtension.class);
        publication = publishing.getPublications().create("mavenJava", MavenPublication.class);

        configureRepository();
        configurePublication();
    }

    protected boolean isPublishDisabled() {
        var disabledRaw = project.findProperty("publication.disabled");
        if (disabledRaw == null) return false;
        return Boolean.parseBoolean(disabledRaw.toString());
    }

    protected void configureRepository() {
        var repository = project.findProperty("deploymentRepository");
        if (repository == null) return;
        var username = project.findProperty("deploymentUsername");
        publishing.getRepositories().maven(maven -> {
            maven.setUrl(repository);
            maven.setName("deployment");
            if (username != null) {
                maven.credentials(credentials -> {
                    credentials.setUsername(username.toString());
                    credentials.setPassword(project.property("deploymentPassword").toString());
                });
            }
        });
    }

    protected void configurePublication() {
        publication.pom(pom -> {
            pom.getName().set(project.getName());
            pom.getDescription().set(project.getName());
            var repoUrl = project.property("publication.repository_url").toString();
            pom.setPackaging("jar");
            pom.getUrl().set(repoUrl);
            pom.licenses(licenses -> {
                licenses.license(license -> {
                    license.getName().set("Apache License, Version 2.0");
                    license.getUrl().set("https://www.apache.org/licenses/LICENSE-2.0");
                    license.getDistribution().set("repo");
                });
            });
            pom.scm(scm -> {
                scm.getUrl().set(repoUrl);
                scm.getConnection().set(project.property("publication.scm").toString());
                scm.getDeveloperConnection().set(scm.getConnection().get());
            });
            pom.issueManagement(issueManagement -> {
                issueManagement.getSystem().set("GitHub");
                issueManagement.getUrl().set(repoUrl + "/issues");
            });

            pom.developers(developers -> {
                developers.developer(developer -> {
                    developer.getId().set("rassafel");
                    developer.getName().set("Sergey Koroblyov");
                    developer.getEmail().set("koroblyov.sa@gmail.com");
                });
            });
        });
        publication.versionMapping(versionMappingStrategy -> {
            versionMappingStrategy.usage("java-api", VariantVersionMappingStrategy::fromResolutionResult);
            versionMappingStrategy.usage("java-runtime", VariantVersionMappingStrategy::fromResolutionResult);
        });
    }
}
