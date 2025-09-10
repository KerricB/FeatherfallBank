plugins {
    java
}

group = "io.owlcraft"
version = "1.0.0"
description = "FeatherfallBank"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven(url = "https://repo.papermc.io/repository/maven-public/") // Paper API
    maven(url = "https://jitpack.io")                               // VaultAPI
    // If you kept WorldGuard, keep this too; otherwise it's fine to omit:
    // maven(url = "https://maven.enginehub.org/repo/")
}

dependencies {
    // Paper/Bukkit API (compileOnly = provided by the server at runtime)
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    // Vault API (for economy hook)
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")

    // If you still need WorldGuard APIs after rollback, uncomment:
    // compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")
    // compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.18")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}
