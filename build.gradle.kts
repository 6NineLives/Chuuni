import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import nu.studer.gradle.jooq.JooqGenerate
import org.jooq.codegen.JavaGenerator
import org.jooq.meta.hsqldb.HSQLDBDatabase
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Logging

plugins {
    kotlin("jvm") version Toolchain.KOTLIN
    kotlin("kapt") version Toolchain.KOTLIN
    kotlin("plugin.serialization") version Toolchain.KOTLIN
    id("io.papermc.paperweight.userdev") version Toolchain.PAPER_WEIGHT_USERDEV
    id("com.github.johnrengelman.shadow") version Toolchain.SHADOW
    id("net.minecrell.plugin-yml.bukkit") version Toolchain.PLUGIN_YML
    id("dev.s7a.gradle.minecraft.server") version Toolchain.MINECRAFT_SERVER
    id("org.flywaydb.flyway") version Toolchain.FLYWAY
    id("nu.studer.jooq") version Toolchain.JOOQ_GEN
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.hsqldb:hsqldb:${Dependencies.HSQLDB}")
    }
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "me.abhigya"
    version = "1.0"

    kotlin {
        jvmToolchain(JAVA_VERSION)
        sourceSets.main {
            kotlin.srcDir("build/generated/sources/i18n")
        }
    }

    tasks {
        compileKotlin {
            kotlinOptions.suppressWarnings = true
            kotlinOptions.jvmTarget = JAVA_VERSION.toString()
            kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        }
    }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.rapture.pw/repository/maven-releases/")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://jitpack.io")
    maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
    // Platform
    compileOnly(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:${Dependencies.KOTLIN_COROUTINES}"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("com.github.stephanenicolas.toothpick:ktp:${Dependencies.TOOTHPICK}")
    kapt("com.github.stephanenicolas.toothpick:toothpick-compiler:${Dependencies.TOOTHPICK}")
    paperweight.paperDevBundle(Dependencies.MINECRAFT)

    compileOnly(fileTree(mapOf("dir" to "${rootProject.rootDir}/lib", "include" to listOf("*.jar"))))

    //Database
    compileOnly("org.flywaydb:flyway-core:${Toolchain.FLYWAY}")
    compileOnly("org.jooq:jooq:${Dependencies.JOOQ}")
    compileOnly("org.jooq:jooq-kotlin:${Dependencies.JOOQ}")
    compileOnly("com.zaxxer:HikariCP:${Dependencies.HIKARI}")
    jooqGenerator("org.hsqldb:hsqldb:${Dependencies.HSQLDB}")

    //Others
    compileOnly("net.kyori:adventure-text-minimessage:${Dependencies.ADVENTURE}")
    compileOnly("net.kyori:adventure-text-serializer-plain:${Dependencies.ADVENTURE}")
    compileOnly("com.charleskorn.kaml:kaml:${Dependencies.KAML}")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("com.github.retrooper.packetevents:spigot:${Dependencies.PACKET_EVENTS}")
    compileOnly("io.lumine:Mythic-Dist:${Dependencies.MYTHIC_DIST}")
    compileOnly("net.Indyuce:MMOCore:${Dependencies.MMO_CORE}")
//    compileOnly("org.roaringbitmap:RoaringBitmap:${Dependencies.ROARING_BITMAP}")
    implementation("xyz.xenondevs.invui:invui-core:${Dependencies.INVUI}")
    implementation("xyz.xenondevs.invui:inventory-access-r17:${Dependencies.INVUI}")
    implementation("xyz.xenondevs.invui:invui-kotlin:${Dependencies.INVUI}")
    implementation("com.github.Revxrsal.Lamp:common:${Dependencies.LAMP}")
    implementation("com.github.Revxrsal.Lamp:bukkit:${Dependencies.LAMP}")
//    implementation("com.github.megavexnetwork.scoreboard-library:scoreboard-library-implementation:${Dependencies.SCOREBOARD_LIBRARY}")
//    implementation("com.github.megavexnetwork.scoreboard-library:scoreboard-library-modern:${Dependencies.SCOREBOARD_LIBRARY}")

    // Test
//    testImplementation(kotlin("test"))
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Dependencies.KOTLIN_COROUTINES}")
//    testImplementation("com.charleskorn.kaml:kaml:${Dependencies.KAML}")
//    testImplementation("net.kyori:adventure-text-minimessage:${Dependencies.ADVENTURE}")
}

val databaseUrl = "jdbc:hsqldb:file:${project.buildDir}/schema-gen/database;shutdown=true"
flyway {
    driver = "org.hsqldb.jdbc.JDBCDriver"
    user = "SA"
    password = ""
    url = databaseUrl
    validateMigrationNaming = true
    cleanOnValidationError = true
    table = "schema_history"
    placeholders = mapOf(
        "table_prefix" to "",
        "uuidtype" to "UUID",
        "options" to ""
    )
}

jooq {
    version.set(Dependencies.JOOQ)

    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)

            jooqConfiguration.apply {
                logging = Logging.WARN
                jdbc.apply {
                    driver = "org.hsqldb.jdbc.JDBCDriver"
                    url = databaseUrl
                    user = "SA"
                    password = ""
                }

                generator.apply {
                    name = JavaGenerator::class.java.canonicalName
                    database.apply {
                        name = HSQLDBDatabase::class.java.canonicalName
                        inputSchema = "PUBLIC"
                        includes = ".*"
                        excludes = "(?i:information_schema\\\\..*)|(?i:system_lobs\\\\..*)"
                        schemaVersionProvider = "SELECT :schema_name || '_' || MAX(\"version\") FROM \"schema_history\""
                        forcedTypes = listOf(
                            ForcedType()
                                .withUserType("java.util.UUID")
                                .withBinding("me.abhigya.chuunicore.database.binding.UUIDBinding")
                                .withIncludeExpression(".*\\.(UUID)\$")
                                .withIncludeTypes("^UUID\$"),
                            ForcedType()
                                .withUserType("kotlin.time.Duration")
                                .withConverter("me.abhigya.chuunicore.database.binding.DurationConverter")
                                .withIncludeExpression(".*\\.(TIME_PLAYED)\$")
                                .withIncludeTypes(".*")
                        )
                        generate.withJavadoc(true)
                            .withComments(true)
                            .withDaos(true)
                            .withPojos(false)
                        target.withPackageName("me.abhigya.chuunicore.jooq.codegen")
                            .withDirectory("${project.buildDir}/schema-gen/jooq")
                    }
                }
            }
        }
    }
}

tasks {
    build {
        dependsOn(*subprojects.map { it.tasks.build }.toTypedArray())
    }

    assemble {
        dependsOn(reobfJar)
        dependsOn(shadowJar)
    }

    compileKotlin {
        kotlinOptions.suppressWarnings = true
        kotlinOptions.jvmTarget = JAVA_VERSION.toString()
        kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        dependsOn(flywayMigrate)
        dependsOn("generateJooq")
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }

    shadowJar {
        archiveFileName.set("${project.name}.jar")

        val relocatePath = "me.abhigya.chuunicore.libs"
        relocate("toothpick", "$relocatePath.toothpick")
        relocate("xyz.xenondevs.invui", "$relocatePath.inventoryframework.invui")
        relocate("xyz.xenondevs.inventoryaccess", "$relocatePath.inventoryframework.inventoryaccess")
        relocate("revxrsal.commands", "$relocatePath.lamp.commands")
        relocate("net.megavex.scoreboardlibrary", "$relocatePath.scoreboardlibrary")

        exclude("com/google/errorprone/annotations/**")
        exclude("DebugProbesKt.bin")
        exclude("META-INF/**")
        val localVer = arrayOf("fr", "it", "pt")
        for (ver in localVer) {
            exclude("lamp-bukkit_$ver.properties")
            exclude("lamp_$ver.properties")
        }

        dependencies {
            exclude(dependency("org.jetbrains.kotlin:.*:.*"))
            exclude(dependency("org.jetbrains.kotlinx:.*:.*"))
            exclude(dependency("org.jetbrains:annotations:.*"))
            exclude(dependency("org.jooq:.*:.*"))
            exclude(dependency("org.reactivestreams:reactive-streams:.*"))
            exclude(dependency("io.r2dbc:r2dbc-spi:.*"))
        }
    }

    jar {
        enabled = false
    }

    named<JooqGenerate>("generateJooq") {
        allInputsDeclared.set(true)
        outputs.upToDateWhen {
            buildDir.resolve("schema-gen").lastModified() <= buildDir.resolve("schema-gen/jooq").lastModified()
        }
    }

//    test {
//        useJUnitPlatform()
//    }

    task<LaunchMinecraftServerTask>("runServer") {
        dependsOn(build)

        doFirst {
            copy {
                from(buildDir.resolve("libs/${project.name}.jar"))
                into(buildDir.resolve("MinecraftServer/plugins"))
            }
        }

        serverDirectory.set("runServer")
        jvmArgument.add("-Denvironment=development")
        jvmArgument.add("-Dkotlinx.coroutines.debug=on")
        jarUrl.set(LaunchMinecraftServerTask.JarUrl.Paper("1.20.2"))
        agreeEula.set(true)

        outputs.upToDateWhen { false }
    }
}

kapt.includeCompileClasspath = false
flyway.cleanDisabled = false

bukkit {
    main = "me.abhigya.chuunicore.ChuuniCorePlugin"
    name = "ChuuniCore"
    version = "1.0"
    authors = listOf("Abhigya")
    description = "Chuuni Core plugin"
    apiVersion = "1.13"
    libraries = listOf(
        "org.jetbrains.kotlin:kotlin-stdlib:${Toolchain.KOTLIN}",
        "org.jetbrains.kotlin:kotlin-stdlib-common:${Toolchain.KOTLIN}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${Dependencies.KOTLIN_COROUTINES}",
        "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:${Dependencies.KOTLIN_SERIALIZE}",
        "org.jetbrains:annotations:${Dependencies.JETBRAINS_ANNOTATIONS}",
        "net.kyori:adventure-text-minimessage:${Dependencies.ADVENTURE}",
        "net.kyori:adventure-text-serializer-plain:${Dependencies.ADVENTURE}",
        "com.charleskorn.kaml:kaml-jvm:${Dependencies.KAML}",
        "org.flywaydb:flyway-core:${Toolchain.FLYWAY}",
        "org.jooq:jooq:${Dependencies.JOOQ}",
        "org.jooq:jooq-kotlin:${Dependencies.JOOQ}",
        "com.zaxxer:HikariCP:${Dependencies.HIKARI}",
        "org.hsqldb:hsqldb:${Dependencies.HSQLDB}",
        "org.mariadb.jdbc:mariadb-java-client:${Dependencies.MARIADB}",
        "org.postgresql:postgresql:${Dependencies.POSTGRESQL}"
    )
    softDepend = listOf(
        "PlaceholderAPI",
        "packetevents"
    )
}