package com.github.uteeaami.model

/**
 * Middle-level DTO between entity and API model
 */
data class TopSender(
    val email: String,
    val messageCount: Long,
)

fun TopSender.toSender(): Sender =
    Sender(
        email = email,
        sentMessages = messageCount,
    )
