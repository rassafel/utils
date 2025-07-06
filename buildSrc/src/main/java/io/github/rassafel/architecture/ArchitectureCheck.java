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

package io.github.rassafel.architecture;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.function.Function;

import com.tngtech.archunit.base.HasDescription;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import com.tngtech.archunit.lang.FailureMessages;
import com.tngtech.archunit.lang.FailureReport;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.*;

import static io.github.rassafel.architecture.ArchitectureRules.*;

public abstract class ArchitectureCheck extends DefaultTask {
    public static final int LIMIT_VIOLATIONS = 10;
    public static final int LIMIT_MESSAGES = 10;

    private FileCollection classes;

    public ArchitectureCheck() {
        getOutputFile().convention(getProject()
            .getLayout()
            .getBuildDirectory()
            .dir(getName())
            .map(dir -> dir.file("failure-report.txt"))
        );
        getRules().addAll(
            packageInfoShouldBeNullMarked(),
            noClassesShouldCallStringToLowerCaseWithoutLocale(),
            noClassesShouldCallStringToUpperCaseWithoutLocale(),
            nullableAnnotation(),
            nonNullAnnotation()
        );
        getRuleDescriptions().set(getRules().map((rules) -> rules.stream().map(ArchRule::getDescription).toList()));
    }

    @TaskAction
    void checkArchitecture() throws IOException {
        var violations = evaluateViolations();
        var outputPath = getOutputFile().get().getAsFile().toPath();
        Files.createDirectories(outputPath.getParent());
        if (violations.isEmpty()) {
            Files.deleteIfExists(outputPath);
            return;
        }
        writeViolations(violations, outputPath);
        throw new GradleException(exceptionMessage(violations) + "\nSee '" + outputPath + "' for details.");
    }

    private Collection<EvaluationResult> evaluateViolations() {
        var javaClasses = new ClassFileImporter()
            .importPaths(getClasses().getFiles().stream().map(File::toPath).toList());
        return getRules().get()
            .stream()
            .map((rule) -> rule.evaluate(javaClasses))
            .filter(EvaluationResult::hasViolation)
            .toList();
    }

    private void writeViolations(Collection<EvaluationResult> violations, Path outputPath) throws IOException {
        try (var writer = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (var violation : violations) {
                writer.write(violation.getFailureReport().toString());
                writer.newLine();
            }
        }
    }

    private String exceptionMessage(Collection<EvaluationResult> violations) {
        var limitedViolations = violations.stream().limit(LIMIT_VIOLATIONS).toList();
        var builder = new StringBuilder("Architecture check failed. Architecture violations (")
            .append(violations.size()).append("times):\n");

        var failureMessagesGet = getFieldValueSupplier("failureMessages", FailureReport.class, FailureMessages.class);
        var ruleGet = getFieldValueSupplier("rule", FailureReport.class, HasDescription.class);

        for (var violation : limitedViolations) {
            var report = violation.getFailureReport();
            var failureMessages = failureMessagesGet.apply(report);
            var limitedMessages = failureMessages.stream().limit(LIMIT_MESSAGES).toList();
            builder.append(String.format(
                "  - Architecture Violation [Priority: %s] - Rule '%s' was violated (%s times):",
                violation.getPriority().asString(), ruleGet.apply(report),
                failureMessages.size()));
            for (var message : limitedMessages) {
                builder.append("\n  * ").append(message.replace("\n", "  *- "));
            }
            var remainMessages = failureMessages.size() - limitedMessages.size();
            if (remainMessages > 0) {
                builder.append("\n  * Remains violation failure: ").append(remainMessages);
            }
        }
        return builder.toString();
    }

    private <O, R> Function<O, R> getFieldValueSupplier(String fieldName, Class<O> owner, Class<R> fieldType) {
        try {
            var field = owner.getDeclaredField(fieldName);
            if (!fieldType.isAssignableFrom(field.getType())) {
                throw new RuntimeException();
            }
            field.setAccessible(true);
            return target -> {
                try {
                    return fieldType.cast(field.get(target));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Internal
    public FileCollection getClasses() {
        return this.classes;
    }

    public void setClasses(FileCollection classes) {
        this.classes = classes;
    }

    @InputFiles
    @SkipWhenEmpty
    @IgnoreEmptyDirectories
    @PathSensitive(PathSensitivity.RELATIVE)
    final FileTree getInputClasses() {
        return this.classes.getAsFileTree();
    }

    @Optional
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getResourcesDirectory();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @Internal
    public abstract ListProperty<ArchRule> getRules();

    @Input
    // The rules themselves can't be an input as they aren't serializable so we use
    // their descriptions instead
    abstract ListProperty<String> getRuleDescriptions();
}
