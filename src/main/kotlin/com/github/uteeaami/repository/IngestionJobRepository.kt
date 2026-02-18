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
        val updated =
            entityManager
                .createQuery(
                    """
            update IngestionJobEntity ij
            set ij.status = 'IN_PROGRESS',
                ij.startedAt = CURRENT_TIMESTAMP,
                ij.finishedAt = null
            where ij.id = :id
              and (
                   ij.status in ('IDLE','FINISHED','FAILED')
                   or
                   (ij.status = 'IN_PROGRESS'
                    and ij.startedAt < :timeout)
              )
            """,
                ).setParameter("id", config.ingestionJob.id())
                .setParameter("timeout", OffsetDateTime.now().minusSeconds(config.ingestionJob.timeoutSeconds()))
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
