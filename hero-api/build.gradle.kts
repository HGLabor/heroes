
loom {
    accessWidenerPath.set(file("src/main/resources/hero-api.accesswidener"))
}

dependencies {
    api(project(":datatracker", configuration = "namedElements"))

    modApi(libs.bundles.fabric)
    modApi(libs.bundles.silk)
    modApi(libs.bundles.performance)
    modApi(libs.owolib)
    modApi(libs.npcLibApi)
    modApi(libs.npcLibCommon)
    modApi(libs.geckolib)
    modApi(libs.playerAnimator)
    modApi(libs.emoteLib)

    modImplementation(files("../libs/npc-lib-fabric-3.0.0-SNAPSHOT.jar"))
// modApi("io.wispforest:owo-lib:0.12.15+1.21")
   // include(implementation("com.thedeanda:lorem:2.2")!!)
    //modCompileOnly(files("../libs/nvidium-0.3.1.jar"))
    //modCompileOnly(files("../libs/sodium-fabric-0.5.11+mc1.21.jar"))
    //(modImplementation(files("../libs/npc-lib-fabric-3.0.0-SNAPSHOT.jar"))!!)
    //implementation("io.github.juliarn", "npc-lib-api", "3.0.0-beta10")
    //implementation("io.github.juliarn", "npc-lib-common", "3.0.0-beta10")
}