pluginManagement {
	repositories {
		mavenCentral()
		maven(url = "https://maven.fabricmc.net/") {
			name = "Fabric"
		}
		gradlePluginPortal()
	}
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "heroes"
include(":datatracker")
include(":hero-api")
include(":katara")
include(":aang")
include(":toph")
include(":ffa-server")

