package com.github.uteeaami.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.enterprise.context.ApplicationScoped
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException

private val logger = KotlinLogging.logger {}

@ApplicationScoped
class ArchiveProcessor {
    companion object {
        private const val FROM_PREFIX = "From:"
    }

    fun process(
        datasetPath: String,
        onUserDirectoryProcessed: (userDirectory: String, entryBySender: Map<String, String>) -> Unit,
    ) {
        val file = File(datasetPath)
        if (!file.exists()) {
            logger.error { "File $datasetPath does not exist" }
            throw FileNotFoundException("Dataset not found: $datasetPath")
        }

        GzipCompressorInputStream(file.inputStream()).use { gzip ->
            TarArchiveInputStream(gzip).use { tar ->

                var currentUserDirectory: String? = null
                val entryBySender = mutableMapOf<String, String>()

                var entry = tar.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val entryUserDirectory = extractUserDirectory(entry.name)

                        if (currentUserDirectory != null && entryUserDirectory != currentUserDirectory) {
                            onUserDirectoryProcessed(currentUserDirectory, entryBySender)
                            entryBySender.clear()
                        }

                        currentUserDirectory = entryUserDirectory

                        val reader = tar.bufferedReader()
                        extractSender(reader)?.let { sender ->
                            entryBySender[entry.name] = sender
                        }
                    }

                    entry = tar.nextEntry
                }

                currentUserDirectory?.let {
                    onUserDirectoryProcessed(it, entryBySender)
                }
            }
        }
    }

    private fun extractSender(reader: BufferedReader): String? {
        while (true) {
            val line = reader.readLine() ?: return null
            if (line.startsWith(FROM_PREFIX)) {
                return line.removePrefix(FROM_PREFIX).trim()
            }
        }
    }

    private fun extractUserDirectory(path: String): String = path.split("/").getOrElse(1) { "unknown" }
}
