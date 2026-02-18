package com.github.uteeaami.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.NotFoundException
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.BufferedReader
import java.io.File

private val logger = KotlinLogging.logger {}

@ApplicationScoped
class ArchiveProcessor {
    companion object {
        private const val FROM_PREFIX = "From:"
    }

    fun process(
        datasetPath: String,
        onUserProcessed: (userName: String, emailToSender: Map<String, String>) -> Unit,
    ) {
        val file = File(datasetPath)
        if (!file.exists()) {
            logger.error { "File $datasetPath does not exist" }
            throw NotFoundException("Dataset not found: $datasetPath")
        }

        GzipCompressorInputStream(file.inputStream()).use { gzip ->
            TarArchiveInputStream(gzip).use { tar ->

                var currentUserDirectory: String? = null
                val emailBySender = mutableMapOf<String, String>()

                var entry = tar.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val entryUserDirectory = extractUserDirectory(entry.name)

                        if (currentUserDirectory != null && entryUserDirectory != currentUserDirectory) {
                            onUserProcessed(currentUserDirectory, emailBySender)
                            emailBySender.clear()
                        }

                        currentUserDirectory = entryUserDirectory

                        val reader = tar.bufferedReader()
                        extractSender(reader)?.let { sender ->
                            emailBySender[entry.name] = sender
                        }
                    }

                    entry = tar.nextEntry
                }

                currentUserDirectory?.let {
                    onUserProcessed(it, emailBySender)
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
