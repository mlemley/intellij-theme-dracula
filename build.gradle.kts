import org.jetbrains.changelog.closure
import org.jetbrains.intellij.tasks.RunPluginVerifierTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.4.0"
    id("org.jetbrains.intellij") version "1.0"
    id("org.jetbrains.changelog") version "1.1.2"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()
}

intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
}

changelog {
    version = properties("pluginVersion")
    path = "${project.projectDir}/CHANGELOG.md"
    header = closure { version }
    itemPrefix = "-"
    keepUnreleasedSection = false
    groups = emptyList()
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        val description = """
            <div>
              <p>Dracula Theme for JetBrains</p>
              <br />
              <p>
                <img
                  alt="Screenshot"
                  src="https://raw.githubusercontent.com/mlemley/intellij-theme-dracula/main/screenshot.jpg"
                  width="600"
                />
              </p>
              <h2>Dracula [-_-]</h2> 
              <p>
                extended from: <a href="https://draculatheme.com/jetbrains">draculatheme.com/jetbrains</a>.
              </p>
              <p>
                Intended for terminal emulators, code editors, and syntax highlighters. Designed to
                be aesthetically pleasing while keeping you focused.
              </p>
              <p><a href="https://gumroad.com/a/477820019">Get it now</a></p>
              <h2>Licence</h2>
              <p>
                <a href="https://raw.githubusercontent.com/dracula/jetbrains/master/LICENSE"
                  >MIT Licence</a
                >
              </p>
              <h2>Extended From</h2>
              <p>
                <a href="https://draculatheme.com/jetbrains">draculatheme.com/jetbrains</a>.
              </p>
            </div>
        """.trimIndent()

        pluginDescription.set(description)
        changeNotes.set(provider { changelog.getLatest().toHTML() })
    }

    runPluginVerifier {
        ideVersions.set(
                properties("pluginVerifierIdeVersions")
                        .split(",")
                        .map(String::trim)
                        .filter(String::isNotEmpty)
        )
        failureLevel.set(
                listOf(
                        RunPluginVerifierTask.FailureLevel.COMPATIBILITY_PROBLEMS,
                        RunPluginVerifierTask.FailureLevel.INVALID_PLUGIN
                )
        )
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getProperty("jetbrains.token"))
    }
}