/*
 * SPDX-FileCopyrightText: (c) 2022 Artyom IG <github.com/rtmigo>
 * SPDX-License-Identifier: Apache-2.0
 */


@file:OptIn(Experimental::class)

import io.github.rtmigo.jasecache.*
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.collections.getOrPut

class FileCacheTest {
    @Test
    fun storeIntegers() {
        withTempDir { tempDir ->
            val cacheDir = tempDir.resolve("theCache")
            JaseFileCache.inDir<String, Int>(cacheDir).use { fileCache ->
                fileCache.put("A", 5)
                fileCache.put("B", 23)
            }

            JaseFileCache.inDir<String, Int>(cacheDir).use { fileCache ->
                fileCache["A"].shouldBe(5)
                fileCache["B"].shouldBe(23)
                fileCache["C"].shouldBeNull()
            }
        }
    }

    @Test
    fun storeDataclasses() {
        withTempDir { tempDir ->
            val cacheDir = tempDir.resolve("theCache")
            JaseFileCache.inDir<Int, SampleClass>(cacheDir).use { fileCache ->
                fileCache.put(10, SampleClass("Masha", setOf("small", "pretty")))
                fileCache.put(12, SampleClass("Pasha", setOf("big", "handsome")))
            }

            JaseFileCache.inDir<Int, SampleClass>(cacheDir).use { fileCache ->
                fileCache[12]!!.name.shouldBe("Pasha")
                fileCache[12]!!.tags.shouldContain("handsome")
            }
        }
    }

    @Test
    fun getOrPut() {
        withTempDir { tempDir ->
            val cacheDir = tempDir.resolve("getOrPut")
            JaseFileCache.inDir<String, Int>(cacheDir).use { fileCache ->
                fileCache.getOrPut("A") { 5 }.shouldBe(5)
                fileCache["A"].shouldBe(5)
            }
        }
    }

    @Test
    fun redingBeforeClosing() {
        withTempDir { tempDir ->
            val cacheDir = tempDir.resolve("theCache")
            JaseFileCache.inDir<String, Int>(cacheDir).use { fileCache ->
                fileCache["A"].shouldBeNull()
                fileCache.put("A", 5)
                fileCache["A"].shouldBe(5)
            }
        }
    }

    @Test
    fun indexingOperators() {
        withTempDir { tempDir ->
            val cacheDir = tempDir.resolve("theCache")
            JaseFileCache.inDir<String, Int>(cacheDir).use { fileCache ->
                fileCache["Answer"].shouldBeNull()
                fileCache["Answer"] = 42
                fileCache["Answer"].shouldBe(42)
            }
        }
    }
}

private data class SampleClass(val name: String, val tags: Set<String>) : java.io.Serializable