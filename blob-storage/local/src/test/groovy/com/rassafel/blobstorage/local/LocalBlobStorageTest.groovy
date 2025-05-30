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

package com.rassafel.blobstorage.local

import java.nio.file.Files

import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import com.rassafel.blobstorage.BlobStorageSpecification
import com.rassafel.blobstorage.core.BlobStorage

@Stepwise
class LocalBlobStorageTest extends BlobStorageSpecification {
    @TempDir
    @Shared
    FileSystemFixture fileSystemFixture

    @Shared
    LocalBlobStorage storage

    def setupSpec() {
        fileSystemFixture.create {
            dir("storagePath") {
            }
        }
        this.storage = new LocalBlobStorage(keyGen, fileSystemFixture.resolve("storagePath").toString(),
                "document", "metadata", clock)
    }

    @Override
    BlobStorage storage() {
        return this.storage
    }

    @Override
    def checkBeforeStoreCheckExists() {
        Files.isDirectory(fileSystemFixture.resolve("storagePath").resolve("document"))
        Files.isDirectory(fileSystemFixture.resolve("storagePath").resolve("metadata").resolve("document"))
        directoriesHasFiles(0)
    }

    @Override
    def checkBeforeStoreUpdate() {
        directoriesHasFiles(0)
    }

    @Override
    def checkBeforeStoreAndUpdateCheckExists() {
        directoriesHasFiles(0)
    }

    @Override
    def checkStore() {
        directoriesHasFiles(1)
    }

    @Override
    def checkAfterStoreCheckExists() {
        directoriesHasFiles(1)
    }

    @Override
    def checkUpdateAfterStore() {
        directoriesHasFiles(1)
    }

    @Override
    def checkAfterUpdateCheckExists() {
        directoriesHasFiles(1)
    }

    @Override
    def checkDelete() {
        directoriesHasFiles(0)
    }

    @Override
    def checkAfterDeleteCheckExists() {
        directoriesHasFiles(0)
    }

    @Override
    def checkAfterDeleteAgainDelete() {
        directoriesHasFiles(0)
    }

    @Override
    def checkAfterDeleteUpdate() {
        directoriesHasFiles(0)
    }

    def "outside check"() {
        given:
        fileSystemFixture.create {
            dir("outside") {
                file("test.txt").write("system data")
            }
            dir("storagePath") {
                dir("document") {
                    file("MWjZsKF").write("Accumsanluctus")
                }
                dir("metadata") {
                    dir("document") {
                        file("MWjZsKF.metadata.properties").write("""
                        Original-Name=MWjZsKF.txt
                        Content-Type=text/plain
                        Content-Length=14
                        Uploaded-At=2025-05-29T19:21:31
                        Last-Modified-At=2025-05-29T19:25:17
                        """.stripIndent())
                    }
                }
            }
        }

        expect:
        existsCheck(false, "../../outside/test.txt")
        existsCheck(false, "../document/MWjZsKF")
        existsCheck(false, "../metadata/MWjZsKF.metadata.properties")
        existsCheck(true, "/MWjZsKF")

        cleanup:
        fileSystemFixture.create {
            dir("outside").deleteDir()
            dir("storagePath") {
                dir("document") {
                    Files.delete(file("MWjZsKF"))
                }
                dir("metadata") {
                    dir("document") {
                        Files.delete(file("MWjZsKF.metadata.properties"))
                    }
                }
            }
        }
    }

    def directoriesHasFiles(int files) {
        def storage = fileSystemFixture.resolve("storagePath")
        def document = storage.resolve("document")
        def metadata = storage.resolve("metadata").resolve("document")

        Files.list(document).count() == files && Files.list(metadata).count() == files
    }
}
