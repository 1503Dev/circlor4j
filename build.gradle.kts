plugins {
	id("net.fabricmc.fabric-loom")
	`maven-publish`
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

repositories {
	maven {
		url = uri("https://maven.minecraftforge.net/")
	}
	maven {
		url = uri("https://repo.spongepowered.org/maven")
	}
	maven {
		url = uri("https://maven.architectury.dev/")
	}
}

loom {
	splitEnvironmentSourceSets()
	mods {
		register("circlor4j") {
			sourceSet(sourceSets.main.get())
			sourceSet(sourceSets.getByName("client"))
		}
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
	implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

//	implementation("net.minecraftforge:forge:26.2-65.1.1")
//	annotationProcessor("net.minecraftforge:eventbus-validator:7.0.5")

	// Fabric API. This is technically optional, but you probably want it anyway.
	//implementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
	compileOnly(files("libs/minecraft.jar"))
	// Source: https://mvnrepository.com/artifact/dev.architectury.loom/dev.architectury.loom.gradle.plugin
//	implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:1.17.491")
//	// Source: https://mvnrepository.com/artifact/architectury-plugin/architectury-plugin.gradle.plugin
//	implementation("architectury-plugin:architectury-plugin.gradle.plugin:3.5.169")
}

tasks.processResources {
	val version = version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 25
}

java {
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)

	from("LICENSE") {
		rename { "${it}_$projectName" }
	}

	manifest {
		attributes(
			"Premain-Class" to "dev1503.circlor4j.Circlor4JavaAgent",
			"Agent-Class" to "dev1503.circlor4j.Circlor4JavaAgent"
		)
	}
}

// configure the maven publication
publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
	}
}
