/*
 * SPDX-FileCopyrightText: (c) 2022 Artyom IG <github.com/rtmigo>
 * SPDX-License-Identifier: Apache-2.0
 */


import io.github.rtmigo.jasecache.systemTempDir
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test
import kotlin.io.path.exists

class TempDirsTest {
    @Test
    fun getTempDir() {
        println(systemTempDir())
        systemTempDir().exists().shouldBeTrue()
    }
}