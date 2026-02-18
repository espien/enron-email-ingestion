package com.github.uteeaami.model

data class Status(
    val status: IngestionStatus,
    val messagesProcessed: Long,
)
