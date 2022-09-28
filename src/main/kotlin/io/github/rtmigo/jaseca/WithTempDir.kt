/*
 * SPDX-FileCopyrightText: (c) 2022 Artyom IG <github.com/rtmigo>
 * SPDX-License-Identifier: Apache-2.0
 */

package io.github.rtmigo.jaseca

import java.nio.file.*

fun withTempDir(block: (tempDir: Path) -> Unit) {
    val tempDir = kotlin.io.path.createTempDirectory()
    try {
        block(tempDir)
    } finally {
        tempDir.toFile().deleteRecursively()
    }
}

fun systemTempDir(): Path
    = Paths.get(System.getProperty("java.io.tmpdir"))