plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
 id("org.jetbrains.kotlin.plugin.compose")
}
android {
 namespace="org.maale.bible"; compileSdk=36
 defaultConfig { applicationId="org.maale.bible"; minSdk=26; targetSdk=36; versionCode=1; versionName="0.1.0"; testInstrumentationRunner="androidx.test.runner.AndroidJUnitRunner" }
 buildFeatures { compose=true }
 compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }
}
kotlin { jvmToolchain(17) }

// Draft learning packages may be used in development builds only. A release build
// fails closed until every bundled package carries explicit release approval.
val verifyLearningContentForRelease by tasks.registering {
    val contentDirectory = layout.projectDirectory.dir("src/main/assets/content/books").asFile
    doLast {
        val packages = contentDirectory.listFiles { file -> file.isFile && file.extension == "json" }
            ?.sortedBy { it.name }
            .orEmpty()
        check(packages.isNotEmpty()) {
            "Release blocked: no learning-content packages were found to validate."
        }
        val unapproved = packages.filter { file ->
            val manifest = groovy.json.JsonSlurper().parse(file) as? Map<*, *>
            manifest?.get("publication_state") != "approved_for_release"
        }
        check(unapproved.isEmpty()) {
            "Release blocked: learning packages lack explicit approval: ${unapproved.joinToString { it.name }}"
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    dependsOn(verifyLearningContentForRelease)
}

dependencies {
 implementation(platform("androidx.compose:compose-bom:2025.12.00"))
 implementation("androidx.activity:activity-compose:1.13.0")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.ui:ui-tooling-preview")
 implementation("androidx.sqlite:sqlite:2.6.2")
 implementation("androidx.sqlite:sqlite-bundled:2.6.2")
 debugImplementation("androidx.compose.ui:ui-tooling")
 testImplementation("junit:junit:4.13.2")
 androidTestImplementation(platform("androidx.compose:compose-bom:2025.12.00"))
 androidTestImplementation("androidx.test.ext:junit:1.3.0")
 androidTestImplementation("androidx.test:runner:1.7.0")
 androidTestImplementation("androidx.test:core:1.7.0")
 androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
 androidTestImplementation("androidx.compose.ui:ui-test-junit4")
 debugImplementation("androidx.compose.ui:ui-test-manifest")
}
