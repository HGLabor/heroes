dependencies {
    implementation(project(":hero-api", configuration = "namedElements"))
    implementation(project(":datatracker", configuration = "namedElements"))

    modApi(libs.bundles.fabric)
    modApi(libs.bundles.silk)
    modApi(libs.bundles.performance)
    modApi(libs.owolib)
    modApi(libs.npcLibApi)
    modApi(libs.npcLibCommon)
    modApi(libs.geckolib)
    modApi(libs.emoteLib)

    modImplementation(files("../libs/npc-lib-fabric-3.0.0-SNAPSHOT.jar"))
}

loom {
    accessWidenerPath.set(file("src/main/resources/katara.accesswidener"))
}

