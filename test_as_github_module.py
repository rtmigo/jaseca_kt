import shutil
import sys
from pathlib import Path

from tempp import *

module = "io.github.rtmigo:jaseca"

url = "https://github.com/rtmigo/jaseca_kt.git"

code = """
    import io.github.rtmigo.jaseca.*
    import java.nio.file.Paths
    
    @OptIn(io.github.rtmigo.jaseca.Experimental::class)

    fun main() {
        filecache<String,Int>("id123").use {
            it["A"] = 123
            require(it["A"] == 123)
            println(it["A"])
        }
    }
"""

try:
    imp_details = """{ version { branch = "__BRANCH__" } }""".replace("__BRANCH__", sys.argv[1])
except IndexError:
    imp_details = ""

with TempProject(
        files={
            # minimalistic build script to use the library
            "build.gradle.kts": """
                plugins {
                    id("application")
                    kotlin("jvm") version "1.7.10"
                }

                repositories { mavenCentral() }
                application { mainClass.set("MainKt") }

                dependencies {
                    implementation("__MODULE__") __IMP_DETAILS__
                }
            """.replace("__MODULE__", module).replace("__IMP_DETAILS__", imp_details),

            # additional settings, if necessary
            "settings.gradle.kts": """
                sourceControl {
                    gitRepository(java.net.URI("__URL__")) { // # .git
                        producesModule("__MODULE__")
                    }
                }
            """.replace("__MODULE__", module).replace("__URL__", url),

            # kotlin code that imports and uses the library
            "src/main/kotlin/Main.kt": code}) as app:
    app.print_files()

    shutil.copytree(Path(__file__).parent/"gradle", app.project_dir/"gradle")
    shutil.copy(Path(__file__).parent/"gradlew", app.project_dir/"gradlew")
    result = app.run([app.project_dir/"gradlew", "run", "--no-daemon", "-q"])

    print("returncode", result.returncode)

    print("stderr", "-" * 80)
    print(result.stderr)

    print("stdout", "-" * 80)
    print(result.stdout)
    print("-" * 80)

    assert result.returncode == 0
    assert result.stdout == "123\n", result.stdout

print("Everything is OK!")
