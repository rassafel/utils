rootProject.name = "utils"

includeProject("bom", "bom")

includeProject("commons", "commons")

includeProject("bus", "bus/bus")
includeProject("bus-starter", "bus/bus-starter")

includeProject("blob-storage-core", "blob-storage/core")
includeProject("blob-storage-event", "blob-storage/event")
includeProject("blob-storage-security", "blob-storage/security")
includeProject("blob-storage-mem", "blob-storage/mem")
includeProject("blob-storage-local", "blob-storage/local")
includeProject("blob-storage-aws", "blob-storage/aws")


fun includeProject(
    name: String, path: String, changeBuildFileName: Boolean = true
) {
    include(":$name")
    project(":$name").apply {
        projectDir = File(settingsDir, path)
        if (changeBuildFileName) {
            buildFileName = "$name.gradle.kts"
        }
    }
}
