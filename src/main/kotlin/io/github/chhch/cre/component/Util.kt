package io.github.chhch.cre.component

import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

internal val Any?.unit get() = Unit

/**
 * @return name of the file without extension
 */
internal fun URL.getFilename() = this.file.substringAfterLast('/').substringBeforeLast('.')

object FileHandler {

    const val COMPONENT_PATH = "user_components" // TODO: add path to parameter

    init {
        Files.createDirectories(Paths.get(COMPONENT_PATH))
    }

    // https://dzone.com/articles/java-nio-vs-io
    fun createFile(file: ByteArray, filename: String?): URL {
        val name = getNonExistingFilename(filename ?: "noName.jar")
        val newFile = File("user_components/$name")
        FileOutputStream(newFile).use {
            it.write(file)
        }
        return newFile.toURI().toURL()
    }

    /**
     * Add '-X' to filename, where X is a number
     *
     * Example: rename `name.jar` to `name-3.jar`, when `name.jar` and `name-2.jar` already exists
     */
    private fun getNonExistingFilename(initialName: String): String {
        var filename = initialName
        if (File("$COMPONENT_PATH/$initialName").exists()) {
            do {
                filename = filename.removeSuffix(".jar")
                val numbersAtEnd =
                        """-\d*$""".toRegex().find(filename)
                                ?.value?.toInt() ?: -1

                filename = filename.removeSuffix(numbersAtEnd.toString())
                filename += "${numbersAtEnd.dec()}.jar"
            } while (File("$COMPONENT_PATH/$filename").exists())
        }
        return filename
    }
}