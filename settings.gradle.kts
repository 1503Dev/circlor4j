import org.gradle.kotlin.dsl.maven

pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		maven {
			url = uri("https://maven.architectury.dev/")
		}
		maven { url = uri("https://files.minecraftforge.net/maven/") }
		mavenCentral()
		gradlePluginPortal()
	}

	plugins {
		id("net.fabricmc.fabric-loom") version providers.gradleProperty("loom_version")
	}
}

// Should match your modid
rootProject.name = "circlor4j"
