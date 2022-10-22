/*
 * SPDX-FileCopyrightText: (c) 2022 Artsiom iG (rtmigo.github.com)
 * SPDX-License-Identifier: ISC
 */

package io.github.rtmigo.jaseca

import java.nio.file.*

internal fun withTempDir(block: (tempDir: Path) -> Unit) {
    val tempDir = kotlin.io.path.createTempDirectory()
    try {
        block(tempDir)
    } finally {
        tempDir.toFile().deleteRecursively()
    }
}

internal fun systemTempDir(): Path
    = Paths.get(System.getProperty("java.io.tmpdir"))