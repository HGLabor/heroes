dependencies {
  api(project(":datatracker", configuration = "namedElements"))

  modApi(libs.bundles.fabric)
  modApi(libs.bundles.silk)
  modApi(libs.bundles.performance)
}
