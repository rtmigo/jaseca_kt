@file:OptIn(Experimental::class)

package samples

import io.github.rtmigo.jasecache.Experimental
import io.github.rtmigo.jasecache.filecache
import java.nio.file.Paths
import kotlin.time.Duration.Companion.minutes

fun main() {

    filecache<String, Int>(Paths.get("/path/to/cache")) {
        // optional configuration block
        maxEntries = 1000
        timeToIdle = 15.minutes
    }
        .use { cache ->
            // use cache
        }
}