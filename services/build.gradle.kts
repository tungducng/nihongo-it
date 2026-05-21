// Declare Kotlin plugin versions once at the root, then have each subproject
// apply them without a version. Without this, the Kotlin Gradle plugin warns
// it has been loaded multiple times across subprojects (one classloader per
// version declaration), which is unsupported.
plugins {
    kotlin("jvm") version "2.3.0" apply false
    kotlin("plugin.spring") version "2.3.0" apply false
    kotlin("plugin.jpa") version "2.3.0" apply false
}
