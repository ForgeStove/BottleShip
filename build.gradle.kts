plugins {
	id("net.neoforged.moddev.legacyforge") version "+"
	id("me.modmuss50.mod-publish-plugin") version "+"
}
base.archivesName.set(p("modName"))
group = p("modGroupId")
version = "${p("minecraftVersion")}-${p("modVersion")}+${p("upperLoader")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
tasks.jar { from("LICENSE") }
var generateMetadata = tasks.register<ProcessResources>("generateMetadata") {
	val values = properties.mapValues { it.value.toString() }
	inputs.properties(values)
	expand(values)
	from("src/main/templates")
	into("build/generated/sources/modMetadata")
}
sourceSets.main.get().resources.srcDir(generateMetadata)
legacyForge {
	version = "${p("minecraftVersion")}-${p("loaderVersion")}"
	parchment {
		mappingsVersion.set(p("parchmentVersion"))
		minecraftVersion.set(p("minecraftVersion"))
	}
	runs {
		create("client").client()
		create("server").server()
		configureEach {
			jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
			systemProperty("terminal.jline", "true")
		}
	}
	mods.create(p("modId")).sourceSet(sourceSets.main.get())
}
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://maven.valkyrienskies.org") // Valkyrien Skies
	maven("https://maven.shedaniel.me") // Cloth Config API
	maven("https://maven.blamejared.com") // JEI
	maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } } // Modrinth
}
dependencies {
	modImplementation("org.valkyrienskies:valkyrienskies-120-${p("loader")}:${p("vsVersion")}")
	compileOnly("org.valkyrienskies.core:api:${p("vsCoreVersion")}") { isTransitive = false }
	compileOnly("org.joml:joml-primitives:${p("jomlVersion")}")
	modImplementation("maven.modrinth:kotlin-for-forge:${p("kotlinForForgeVersion")}")
	modImplementation("me.shedaniel.cloth:cloth-config-${p("loader")}:${p("clothConfigVersion")}")
	modRuntimeOnly("mezz.jei:jei-${p("minecraftVersion")}-${p("loader")}:${p("jeiVersion")}")
}
publishMods {
	file.set(tasks.named("reobfJar").get().outputs.files.singleFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${p("upperLoader")}] ${p("modDisplayName")} ${p("modVersion")}-${p("minecraftVersion")}")
	modLoaders.addAll(p("upperLoader"))
	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("1o5XPZYT")
		minecraftVersions.add(p("minecraftVersion"))
		requires("valkyrien-skies")
	}
}
fun p(key: String) = property(key).toString()
