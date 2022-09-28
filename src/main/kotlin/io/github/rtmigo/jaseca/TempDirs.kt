package io.github.rtmigo.jaseca

import java.nio.file.Path

private fun isCorrectId(text: String) =
    text.matches("^\\w{1,20}$".toRegex())

class IllegalIdException : IllegalArgumentException(
    "The id must be non-empty alphanumeric ASCII string " +
        "up to 20 characters long.")

fun toTempSubdir(id: String): Path {
    if (!isCorrectId(id))
        throw IllegalIdException()
    val parent = systemTempDir()
    val result = parent.resolve("jfc_$id")
    if (result.parent != parent)
        throw Error("The created directory $result is not a child of $parent")
    return result
}