package com.github.uteeaami.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "ingestion_job")
data class IngestionJobEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: IngestionStatusEntity = IngestionStatusEntity.IDLE,
    @Column(name = "started_at")
    var startedAt: OffsetDateTime? = null,
    @Column(name = "finished_at")
    var finishedAt: OffsetDateTime? = null,
)
