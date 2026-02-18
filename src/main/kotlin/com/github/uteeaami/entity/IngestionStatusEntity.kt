package com.github.uteeaami.entity

import com.github.uteeaami.model.IngestionStatus

enum class IngestionStatusEntity {
    IDLE,
    IN_PROGRESS,
    FINISHED,
    FAILED,
}

fun IngestionStatusEntity.toIngestionStatus(): IngestionStatus =
    when (this) {
        IngestionStatusEntity.IDLE -> IngestionStatus.IDLE
        IngestionStatusEntity.IN_PROGRESS -> IngestionStatus.IN_PROGRESS
        IngestionStatusEntity.FINISHED -> IngestionStatus.FINISHED
        IngestionStatusEntity.FAILED -> IngestionStatus.FAILED
    }
