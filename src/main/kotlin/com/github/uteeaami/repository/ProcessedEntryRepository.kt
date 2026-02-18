package com.github.uteeaami.repository

import com.github.uteeaami.config.IngestionConfig
import com.github.uteeaami.entity.ProcessedEntryEntity
import com.github.uteeaami.model.TopSender
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional

@ApplicationScoped
class ProcessedEntryRepository(
    private val entityManager: EntityManager,
    private val config: IngestionConfig,
) {
    @Transactional
    fun persistEntries(entries: List<ProcessedEntryEntity>) {
        val batchSize = config.ingestionJob.batchSize()

        entries.forEachIndexed { index, entity ->
            entityManager.persist(entity)

            if (index % batchSize == 0) {
                entityManager.flush()
                entityManager.clear()
            }
        }
    }

    fun getTotalProcessedEntries(): Long =
        entityManager
            .createQuery("select count(pe) from ProcessedEntryEntity pe", Long::class.java)
            .singleResult

    fun getTopSenders(limit: Int): List<TopSender> =
        entityManager
            .createQuery(
                """
            select pe.senderEmail, count(pe)
            from ProcessedEntryEntity pe
            group by pe.senderEmail
            order by count(pe) desc
            """,
                TopSender::class.java,
            ).setMaxResults(limit)
            .resultList

    /**
     * Fetches the names of entries that have already been processed.
     * This is used to avoid re-processing entries that are already in the database.
     */
    @Transactional
    fun getTotalProcessedEntryNamesByNames(entryName: List<String>): Set<String> =
        entityManager
            .createQuery(
                "select pe.entryName from ProcessedEntryEntity pe where pe.entryName in :entryNames",
                String::class.java,
            ).setParameter("entryNames", entryName)
            .resultList
            .toSet()

    @Transactional
    fun reset() {
        entityManager.createQuery("DELETE FROM ProcessedEntryEntity").executeUpdate()
    }
}
