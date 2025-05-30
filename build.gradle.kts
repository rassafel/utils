import org.jetbrains.gradle.ext.EncodingConfiguration
import org.jetbrains.gradle.ext.copyright
import org.jetbrains.gradle.ext.encodings
import org.jetbrains.gradle.ext.settings

plugins {
    idea
    org.jetbrains.gradle.plugin.`idea-ext`
    io.freefair.`git-version`
//    jacoco
    `test-report-aggregation`
//    io.freefair.`aggregate-jacoco-report`
//    org.barfuin.gradle.jacocolog
}

val isCi = System.getenv("CI")?.isNotBlank() ?: false

idea.module {
    isDownloadSources = !isCi
    isDownloadJavadoc = !isCi
}

idea.project {
    settings {
        copyright {
            useDefault = "DefaultCopyright"
            profiles {
                create("DefaultCopyright") {
                    notice =
                        """Copyright 2024-${'$'}today.year the original author or authors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License."""
                }
            }
        }
        encodings {
            encoding = "UTF-8"
            bomPolicy = EncodingConfiguration.BomPolicy.WITH_NO_BOM
            properties {
                encoding = "UTF-8"
                transparentNativeToAsciiConversion = false
            }
        }
    }
}
