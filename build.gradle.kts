@file:Suppress("SpellCheckingInspection")

plugins {
	idea
	id("net.neoforged.moddev.legacyforge") version "+"
	id("me.modmuss50.mod-publish-plugin") version "+"
}
base.archivesName.set(p("mod_id"))
group = p("mod_group_id")
version = "${p("minecraft_version")}-${p("mod_version")}+${p("upper_loader")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
idea.module {
	isDownloadSources = true
	isDownloadJavadoc = true
}
tasks.jar {
	from("LICENSE")
//	manifest { attributes(mapOf("MixinConfigs" to "${p("mod_id")}.mixins.json")) }
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
legacyForge {
	version = "${p("minecraft_version")}-${p("loader_version")}"
	parchment {
		mappingsVersion.set(p("parchment_version"))
		minecraftVersion.set(p("minecraft_version"))
	}
	runs {
		create("client") { client() }
		configureEach {
			jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
			systemProperty("terminal.jline", "true")
		}
	}
	mods { create(p("mod_id")) { sourceSet(sourceSets["main"]) } }
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
	modImplementation("maven.modrinth:valkyrien-skies:1.20.1-forge-2.3.0-beta.9")
	modImplementation("maven.modrinth:vmod:16KEVTxj")
	modImplementation("maven.modrinth:kotlin-for-forge:4.11.0")
	modImplementation("maven.modrinth:architectury-api:9.2.14+forge")
	compileOnly("org.joml:joml-primitives:1.10.0")
	compileOnly(fileTree("libs"))
	modImplementation("me.shedaniel.cloth:cloth-config-${p("loader")}:${p("cloth_config_version")}")
	modRuntimeOnly("mezz.jei:jei-${p("minecraft_version")}-${p("loader")}:${p("jei_version")}")
	compileOnly("org.jetbrains:annotations:${p("annotations_version")}")
//	compileOnly("io.github.llamalad7:mixinextras-common:${p("mixin_extras_version")}")
//	implementation("io.github.llamalad7:mixinextras-${p("loader")}:${p("mixin_extras_version")}")
//	annotationProcessor("org.spongepowered:mixin:${p("mixin_version")}:processor")
}
publishMods {
	file.set(tasks.named("reobfJar").get().outputs.files.singleFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${p("upper_loader")}] ${p("mod_name")} ${p("mod_version")}+${p("minecraft_version")}")
	modLoaders.addAll(p("upper_loader"))
	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("1o5XPZYT")
		minecraftVersions.add(p("minecraft_version"))
		requires("valkyrien-skies", "vmod")
	}
}
fun p(key: String) = property(key).toString()
