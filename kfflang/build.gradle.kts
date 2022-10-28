import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime

plugins {
    kotlin("jvm")
    id("net.minecraftforge.gradle")
    id("com.modrinth.minotaur") version "2.+" // TODO Move modrinth to main
    `maven-publish`
}

val mc_version: String by project
val forge_version: String by project
val kotlin_version: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

minecraft {
    mappings("official", mc_version)

    runs {
        create("client") {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")

            mods {
                create("kfflang") {
                    source(sourceSets.main.get())
                }

                create("kfflangtest") {
                    source(sourceSets.test.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run/server"))

            property("forge.logging.markers", "SCAN,LOADING,CORE")
            property("forge.logging.console.level", "debug")

            mods {
                create("kfflang") {
                    source(sourceSets.main.get())
                }

                create("kfflangtest") {
                    source(sourceSets.test.get())
                }
            }
        }
    }
}

configurations {
    runtimeElements {
        setExtendsFrom(emptySet())
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:$mc_version-$forge_version")

    // Default classpath
    implementation("org.jetbrains.kotlin", "kotlin-stdlib", kotlin_version)
    implementation("org.jetbrains.kotlin", "kotlin-stdlib-common", kotlin_version)
    implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", kotlin_version)
    implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk7", kotlin_version)
    implementation("org.jetbrains.kotlin", "kotlin-reflect", kotlin_version)
}

tasks {
    withType<Jar> {
        manifest {
            attributes(
                "Specification-Title" to "Kotlin for Forge",
                "Specification-Vendor" to "Forge",
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "thedarkcolour",
                "Implementation-Timestamp" to LocalDateTime.now(),
                "Automatic-Module-Name" to "thedarkcolour.kotlinforforge.lang",
                "FMLModType" to "LANGPROVIDER",
            )
        }
    }

    // Only require the lang provider to use explicit visibility modifiers, not the test mod
    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs = listOf("-Xexplicit-api=warning", "-Xjvm-default=all")
    }
}

// Workaround to remove build\classes\java from MOD_CLASSES because SJH doesn't like nonexistent dirs
setOf(sourceSets.main, sourceSets.test)
    .map(Provider<SourceSet>::get)
    .forEach { sourceSet ->
        val mutClassesDirs = sourceSet.output.classesDirs as ConfigurableFileCollection
        val javaClassDir = sourceSet.java.classesDirectory.get()
        val mutClassesFrom = mutClassesDirs.from
            .filter {
                val toCompare = (it as? Provider<*>)?.get()
                return@filter javaClassDir != toCompare
            }
            .toMutableSet()
        mutClassesDirs.setFrom(mutClassesFrom)
    }

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

modrinth {
    projectId.set("ordsPcFz")
    versionNumber.set(project.version.toString())
    versionType.set("release")
    gameVersions.addAll("1.18", "1.18.1", "1.19")
    loaders.add("forge")
}
