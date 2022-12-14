@file:OptIn(Experimental::class)

import io.github.rtmigo.jaseca.Experimental
import io.github.rtmigo.jaseca.filecache
import java.nio.file.Paths

data class Planet(val radius: Double, val period: Double): java.io.Serializable

fun main() {
    filecache<String, Planet>(Paths.get("/path/to/cache"))
        .use { cache ->
            cache["Mars"] = Planet(389.5,686.980)
            cache["Mercury"] = Planet(2439.7,87.9691)
        }
}


