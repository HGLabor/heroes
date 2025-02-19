version = "1.1.0"

dependencies {
    implementation(project(":hero-api", configuration = "namedElements"))
    implementation(project(":datatracker", configuration = "namedElements"))

    modApi(libs.bundles.fabric)
    modApi(libs.bundles.silk)
    modApi(libs.bundles.performance)
    modApi(libs.owolib)
    modApi(libs.geckolib)
    modApi(libs.emoteLib)
}

loom {
    accessWidenerPath.set(file("src/main/resources/toph.accesswidener"))
}

