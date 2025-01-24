loom {
    accessWidenerPath.set(file("src/main/resources/hero-api.accesswidener"))
}

dependencies {
    api(project(":datatracker", configuration = "namedElements"))

    modApi(libs.bundles.fabric)
    modApi(libs.bundles.silk)
    modApi(libs.bundles.performance)
    modApi(libs.owolib)
    modApi(libs.geckolib)
    modApi(libs.emoteLib)
}