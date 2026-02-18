package com.github.uteeaami.service

import com.github.uteeaami.config.IngestionConfig
import com.github.uteeaami.entity.IngestionStatusEntity
import com.github.uteeaami.entity.ProcessedEntryEntity
import com.github.uteeaami.entity.toIngestionStatus
import com.github.uteeaami.exception.IngestionInProgressException
import com.github.uteeaami.model.Sender
import com.github.uteeaami.model.Status
import com.github.uteeaami.model.toSender
import com.github.uteeaami.repository.IngestionJobRepository
import com.github.uteeaami.repository.ProcessedEntryRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.context.ManagedExecutor
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@ApplicationScoped
class IngestionService(
    private val archiveProcessor: ArchiveProcessor,
    private val config: IngestionConfig,
    private val ingestionJobRepository: IngestionJobRepository,
    private val processedEntryRepository: ProcessedEntryRepository,
    private val managedExecutor: ManagedExecutor,
) {
    fun startIngestion() {
        if (!ingestionJobRepository.tryStart()) throw IngestionInProgressException("Ingestion already started")

        Uni
            .createFrom()
            .item {
                logger.info { "Starting ingestion..." }
                val duration: Long = measureTimeMillis { runIngestion() }
                duration
            }.runSubscriptionOn(managedExecutor)
            .subscribe()
            .with(
                { duration ->
                    logger.info { "Ingestion finished successfully in $duration ms" }
                    ingestionJobRepository.markAs(IngestionStatusEntity.FINISHED)
                },
                { throwable ->
                    logger.error(throwable) { "Ingestion failed with exception: ${throwable.message}" }
                    ingestionJobRepository.markAs(IngestionStatusEntity.FAILED)
                },
            )
    }

    fun currentStatus(): Status {
        val ingestion = ingestionJobRepository.getIngestionJob()
        val totalProcessed = processedEntryRepository.getTotalProcessedEntries()

        return Status(
            status = ingestion.status.toIngestionStatus(),
            messagesProcessed = totalProcessed,
        )
    }

    fun topSenders(limit: Int = 10): List<Sender> = processedEntryRepository.getTopSenders(limit).map { it.toSender() }

    private fun runIngestion() {
        archiveProcessor.process(config.dataSource.path()) { user, emailToSender ->
            if (emailToSender.isNotEmpty()) {
                processUserDirectory(user, emailToSender)
            }
        }
    }

    fun processUserDirectory(
        userDirectory: String,
        emailBySender: Map<String, String>,
    ) {
        val processedEntries =
            processedEntryRepository.getTotalProcessedEntryNamesByNames(emailBySender.keys.toList())

        val newEntries = emailBySender.filterKeys { it !in processedEntries }
        if (newEntries.isEmpty()) {
            logger.info { "No new entries to process for directory '$userDirectory'" }
            return
        }

        val entities =
            newEntries.map { (entryName, senderEmail) ->
                ProcessedEntryEntity(entryName = entryName, senderEmail = senderEmail)
            }

        processedEntryRepository.persistEntries(entities)

        logger.info {
            "Processed directory '$userDirectory': ${newEntries.size} new entries"
        }
    }

    fun reset() {
        logger.info { "Resetting ingestion database..." }
        ingestionJobRepository.reset()
        processedEntryRepository.reset()
        logger.info { "Ingestion reset completed." }
    }
}
