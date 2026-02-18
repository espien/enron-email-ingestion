package com.github.uteeaami.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "processed_entries")
data class ProcessedEntryEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "entry_name", nullable = false)
    val entryName: String = "",
    @Column(name = "sender_email", nullable = false)
    val senderEmail: String = "",
)
