package com.github.uteeaami.repository

import com.github.uteeaami.config.IngestionConfig
import com.github.uteeaami.entity.IngestionJobEntity
import com.github.uteeaami.entity.IngestionStatusEntity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import java.time.OffsetDateTime

@ApplicationScoped
class IngestionJobRepository(
    private val entityManager: EntityManager,
    private val config: IngestionConfig,
) {
    fun getIngestionJob(): IngestionJobEntity =
        entityManager.find(
            IngestionJobEntity::class.java,
            config.ingestionJob.id(),
        )

    @Transactional
    fun tryStart(): Boolean {
        val allowedStatuses =
            listOf(IngestionStatusEntity.IDLE, IngestionStatusEntity.FINISHED, IngestionStatusEntity.FAILED)

        val updated =
            entityManager
                .createQuery(
                    """
            update IngestionJobEntity ij
            set ij.status = :inProgress,
                ij.startedAt = CURRENT_TIMESTAMP,
                ij.finishedAt = null
            where ij.id = :id
              and (
                   ij.status in :allowedStatuses
                   or
                   (ij.status = :inProgress
                    and ij.startedAt < :timeout)
              )
            """,
                ).setParameter("id", config.ingestionJob.id())
                .setParameter("timeout", OffsetDateTime.now().minusSeconds(config.ingestionJob.timeoutSeconds()))
                .setParameter("inProgress", IngestionStatusEntity.IN_PROGRESS)
                .setParameter("allowedStatuses", allowedStatuses)
                .executeUpdate()
        return updated == 1
    }

    @Transactional
    fun markAs(status: IngestionStatusEntity) {
        entityManager
            .createQuery(
                """
            update IngestionJobEntity ij 
            set ij.status = :status, ij.finishedAt = CURRENT_TIMESTAMP 
            where ij.id = :id
            """,
            ).setParameter("id", config.ingestionJob.id())
            .setParameter("status", status)
            .executeUpdate()
    }

    @Transactional
    fun reset() {
        entityManager
            .createQuery(
                """
                update IngestionJobEntity ij
                set ij.status = :status,
                    ij.startedAt = null,
                    ij.finishedAt = null
                where ij.id = :id
            """,
            ).apply {
                setParameter("status", IngestionStatusEntity.IDLE)
                setParameter("id", config.ingestionJob.id())
            }.executeUpdate()
    }
}
