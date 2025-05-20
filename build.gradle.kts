@file:Suppress("SpellCheckingInspection")

plugins {
	idea
	id("net.neoforged.moddev.legacyforge") version "+"
	id("me.modmuss50.mod-publish-plugin") version "+"
//	id("dev.vfyjxf.modaccessor") version "+"
}
base.archivesName.set(e("mod_id"))
group = e("mod_group_id")
version = "${e("minecraft_version")}-${e("mod_version")}+${e("upper_loader")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
idea.module {
	isDownloadSources = true
	isDownloadJavadoc = true
}
tasks.jar {
	from("LICENSE")
//	manifest { attributes(mapOf("MixinConfigs" to "${e("mod_id")}.mixins.json")) }
}
tasks.processResources {
	val replace = properties.mapValues { it.value.toString() }
	inputs.properties(replace)
	from("src/main/resources") {
		include("**/*.toml")
		expand(replace)
	}
	into("build/resources/main")
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
//modAccessor {
//	createTransformConfiguration(configurations.compileOnly.get())
//	accessTransformerFiles = legacyForge.accessTransformers.files
//}
//mixin {
//	add(sourceSets.main.get(), "${e("mod_id")}.refmap.json")
//	config("${e("mod_id")}.mixins.json")
//}
legacyForge {
	version = "${e("minecraft_version")}-${e("loader_version")}"
	parchment {
		mappingsVersion.set(e("parchment_version"))
		minecraftVersion.set(e("minecraft_version"))
	}
	runs {
		create("client") { client() }
		configureEach {
			jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
			systemProperty("terminal.jline", "true")
		}
	}
	mods { create(e("mod_id")) { sourceSet(sourceSets["main"]) } }
}
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://api.modrinth.com/maven") // Modrinth
//	maven("https://maven.valkyrienskies.org") // Valkyrien Skies
	maven("https://maven.shedaniel.me") // Cloth Config API
	maven("https://maven.blamejared.com") // JEI
}
dependencies {
	modImplementation("maven.modrinth:valkyrien-skies:1.20.1-forge-2.3.0-beta.7")
	modImplementation("maven.modrinth:vmod:lK0oLDaV")
	modImplementation("maven.modrinth:kotlin-for-forge:4.11.0")
	modImplementation("maven.modrinth:architectury-api:9.2.14+forge")
	compileOnly(fileTree("libs"))
//	compileOnly("io.github.llamalad7:mixinextras-common:${e("mixin_extras_version")}")
//	implementation("io.github.llamalad7:mixinextras-${e("loader")}:${e("mixin_extras_version")}")
	modImplementation("me.shedaniel.cloth:cloth-config-${e("loader")}:${e("cloth_config_version")}")
	modImplementation("mezz.jei:jei-${e("minecraft_version")}-${e("loader")}:${e("jei_version")}")
//	annotationProcessor("org.spongepowered:mixin:${e("mixin_version")}:processor")
	compileOnly("org.jetbrains:annotations:${e("annotations_version")}")
}
publishMods {
	file.set(tasks.jar.get().outputs.files.singleFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${e("upper_loader")}] ${e("mod_name")} ${e("mod_version")}+${e("minecraft_version")}")
	modLoaders.addAll(e("upper_loader"))
	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("1o5XPZYT")
		minecraftVersions.add(e("minecraft_version"))
		requires("create", "cloth-config")
	}
}
fun e(key: String) = extra[key].toString()
