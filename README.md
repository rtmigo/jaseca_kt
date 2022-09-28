# jasecache [WIP]

# Install

#### settings.gradle.kts

```kotlin
sourceControl {
    gitRepository(java.net.URI("https://github.com/rtmigo/jasecache_kt.git")) {
        producesModule("io.github.rtmigo:jasecache")
    }
}
```

#### build.gradle.kts

```kotlin
dependencies {
    implementation("io.github.rtmigo:jasecache") {
        version { branch = "staging" }
    }
    implementation("org.ehcache:ehcache:3.10.1")
}
```

# Use

### Create, write, read

```kotlin
import io.github.rtmigo.jasecache.filecache
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

### Configure

```kotlin
import io.github.rtmigo.jasecache.filecache
import java.nio.file.Paths
import kotlin.time.Duration.Companion.minutes

fun main() {

    filecache<String, Int>(Paths.get("/path/to/cache_dir")) {
        // optional configuration block
        maxEntries = 1000
        timeToIdle = 15.minutes
    }
        .use { cache ->
            // use cache
        }
}
```

### Save structured data

Just inherit your Kotlin class from `java.io.Serializable` to make it compatible.

```kotlin
import io.github.rtmigo.jasecache.filecache
import java.nio.file.Paths

data class Planet(val radius: Double, val period: Double)
    : java.io.Serializable  // this makes object compatible

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
