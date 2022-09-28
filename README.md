# [jaseca](https://github.com/rtmigo/jaseca_kt)

Easy to use file cache for Kotlin/JVM.

Stores data in a directory on a local drive. Allows you to keep data of any types that are 
`java.io.Serializable`. Uses [Ehcache](https://www.ehcache.org/) internally.

```kotlin
import io.github.rtmigo.jaseca.filecache
import java.nio.file.Paths

fun main() {

    filecache<String, Int>(Paths.get("/path/to/cache_dir"))
        .use { cache ->
            // writing to cache
            cache["first key"] = 10
            cache["second key"] = 20
            // reading from cache
            println(cache["first key"])
        }
}
```

"Jaseca" stands for **JA**va.io.**SE**rializable **CA**che.


# Install

#### settings.gradle.kts

```kotlin
sourceControl {
    gitRepository(java.net.URI("https://github.com/rtmigo/jaseca_kt.git")) {
        producesModule("io.github.rtmigo:jaseca")
    }
}
```

#### build.gradle.kts

```kotlin
dependencies {
    implementation("io.github.rtmigo:jaseca") {
        version { branch = "staging" }
    }
}
```

# Use

### Configure, write, read

```kotlin
import io.github.rtmigo.jaseca.filecache
import java.nio.file.Paths
import kotlin.time.Duration.Companion.minutes

fun main() {

    filecache<String, Int>(Paths.get("/path/to/cache_dir")) {
        // optional configuration block
        maxEntries = 1000
        timeToIdle = 15.minutes
    }
        .use { cache ->
            // writing to cache
            cache["first key"] = 10
            cache["second key"] = 20
            // reading from cache
            println(cache["first key"])
        }
}
```

### Save structured data

Just inherit your Kotlin class from `java.io.Serializable` to make it compatible.

```kotlin
import io.github.rtmigo.jaseca.filecache
import java.nio.file.Paths

data class Planet(val radius: Double, val period: Double)
    : java.io.Serializable  // this makes the object compatible

fun main() {

    filecache<String, Planet>(Paths.get("/path/to/cache_dir"))
        .use { cache ->
            cache["Mars"] = Planet(389.5, 686.980)
            cache["Mercury"] = Planet(2439.7, 87.9691)
        }
}
```

## License

Copyright © 2022 [Artyom IG](https://github.com/rtmigo).
Released under the [Apache-2.0](LICENSE).
