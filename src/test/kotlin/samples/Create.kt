import io.github.rtmigo.jasecache.filecache
import java.nio.file.Paths

fun main() {
    filecache<String, Int>(Paths.get("/path/to/cache"))
        .use { cache ->
            // writing to cache
            cache["first key"] = 10
            cache["second key"] = 20
            // reading from cache
            println(cache["first key"])
        }
}


