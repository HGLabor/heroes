val worldEditVersion: String by project

dependencies {
    api(project(":hero-api", configuration = "namedElements"))
    api(project(":datatracker", configuration = "namedElements"))
    api(project(":katara", configuration = "namedElements"))

    modApi(libs.bundles.fabric)
    modApi(libs.bundles.silk)
    modApi(libs.bundles.performance)
    modApi(libs.owolib)
    modApi(libs.npcLibApi)
    modApi(libs.npcLibCommon)
    modApi(libs.geckolib)
    modApi(libs.emoteLib)
    modCompileOnly(libs.worldedit)

    modImplementation(files("../libs/npc-lib-fabric-3.0.0-SNAPSHOT.jar"))
    // modCompileOnly("com.sk89q.worldedit:worldedit-fabric-mc${worldEditVersion}") // Ändere die Versionsnummer entsprechend der gewünschten Version
}

loom {
    accessWidenerPath.set(file("src/main/resources/ffa-server.accesswidener"))
}



