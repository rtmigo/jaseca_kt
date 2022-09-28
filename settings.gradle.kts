rootProject.name = "jasecache"

pluginManagement.resolutionStrategy.eachPlugin {
    if (requested.id.id.startsWith("org.jetbrains.kotlin.")) {
        useVersion("1.7.10")
    }
}
