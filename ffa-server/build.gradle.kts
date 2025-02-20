version = "1.3.21"

val worldEditVersion: String by project

val includeImplementation: Configuration by configurations.creating {
    configurations.implementation.configure { extendsFrom(this@creating) }
}

dependencies {
    api(project(":hero-api", configuration = "namedElements"))
    api(project(":datatracker", configuration = "namedElements"))
    api(project(":katara", configuration = "namedElements"))
    api(project(":aang", configuration = "namedElements"))
    api(project(":toph", configuration = "namedElements"))

    modApi(libs.bundles.fabric)
    modApi(libs.bundles.silk)
    modApi(libs.bundles.nrc)
    modApi(libs.bundles.performance)
    modApi(libs.owolib)
    modApi(libs.bundles.npcLib)
    modApi(libs.geckolib)
    modApi(libs.emoteLib)
    //modImplementation(libs.bundles.cloudnet)
    modCompileOnly(libs.worldedit)
    modCompileOnly(libs.luckperms)
    includeImplementation(libs.bundles.mongodb)
    modImplementation(libs.hglabor.database.utils) {
        exclude(module = "fabric-api")
        exclude(module = "hglabor-utils-events")
    }
    include(libs.hglabor.database.utils)
    //includeImplementation(libs.bundles.hglaborutils)

    // modCompileOnly("com.sk89q.worldedit:worldedit-fabric-mc${worldEditVersion}") // Ändere die Versionsnummer entsprechend der gewünschten Version

    handleIncludes(includeImplementation)
}

/* Thanks to https://github.com/jakobkmar for this script */
fun DependencyHandlerScope.includeTransitive(
    dependencies: Set<ResolvedDependency>,
    fabricLanguageKotlinDependency: ResolvedDependency?,
    checkedDependencies: MutableSet<ResolvedDependency> = HashSet()
) {
    val minecraftDependencies = listOf(
        "slf4j-api",
        "commons-logging",
        "oshi-core",
        "jna",
        "jna-platform",
        "gson",
        "commons-lang3",
        "jackson-annotations",
        "jackson-core",
        "jackson-databind",
    )

    dependencies.forEach {
        if (checkedDependencies.contains(it) /*|| it.moduleGroup == "org.jetbrains.kotlin" || it.moduleGroup == "org.jetbrains.kotlinx"*/) return@forEach

        if (it.name.startsWith("net.fabric")) {
            checkedDependencies += it
            return@forEach
        }

        if (it.name.startsWith("net.silkmc")) {
            checkedDependencies += it
            return@forEach
        }

        if (fabricLanguageKotlinDependency?.children?.any { kotlinDep -> kotlinDep.name == it.name } == true) {
            println("Skipping -> ${it.name} (already in fabric-language-kotlin)")
        } else if (minecraftDependencies.any { dep -> dep == it.moduleName }) {
            println("Skipping -> ${it.name} (already in minecraft)")
        } else {
            include(it.name)
            println("Including -> ${it.name}")
        }
        checkedDependencies += it

        includeTransitive(it.children, fabricLanguageKotlinDependency, checkedDependencies)
    }
}

fun DependencyHandlerScope.implementAndInclude(dep: Any) {
    modImplementation(dep)
    include(dep)
}

fun DependencyHandlerScope.handleIncludes(configuration: Configuration) {
    includeTransitive(
        configuration.resolvedConfiguration.firstLevelModuleDependencies,
        configurations.modImplementation.get().resolvedConfiguration.firstLevelModuleDependencies
            .firstOrNull() { it.moduleGroup == "net.fabricmc" && it.moduleName == "fabric-language-kotlin" },
    )
}

loom {
    accessWidenerPath.set(file("src/main/resources/ffa-server.accesswidener"))
}
