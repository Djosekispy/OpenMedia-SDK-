plugins {
  alias(libs.plugins.android.library)
  id("maven-publish")
}

android {
  namespace = "org.openmedia.sdk"
  compileSdk = 36

  defaultConfig {
    minSdk = 24
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  publishing {
    singleVariant("release") {
      withSourcesJar()
    }
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }
}

afterEvaluate {
  publishing {
    publications {
      register<MavenPublication>("release") {
        from(components["release"])

        groupId = "org.openmedia"
        artifactId = "openmedia-sdk"
        version = "0.1.0"

        pom {
          name.set("OpenMedia SDK")
          description.set("Modular and reactive Android multimedia playback SDK powered by Media3 and Kotlin Coroutines")
          url.set("https://github.com/openmedia/openmedia-sdk")
          licenses {
            license {
              name.set("The Apache License, Version 2.0")
              url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
          }
          developers {
            developer {
              id.set("openmedia")
              name.set("OpenMedia Team")
            }
          }
          scm {
            connection.set("scm:git:github.com/openmedia/openmedia-sdk.git")
            developerConnection.set("scm:git:ssh://github.com/openmedia/openmedia-sdk.git")
            url.set("https://github.com/openmedia/openmedia-sdk")
          }
        }
      }
    }
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.media3.common)

  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
}
