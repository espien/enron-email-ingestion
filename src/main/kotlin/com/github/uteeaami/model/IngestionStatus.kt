package com.github.uteeaami.model

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue

enum class IngestionStatus {
    @JsonEnumDefaultValue
    IDLE,

    IN_PROGRESS,
    FINISHED,
    FAILED,
}
