package com.github.uteeaami

import com.github.uteeaami.entity.ProcessedEntryEntity
import java.util.UUID

object TestConstants {
    object Emails {
        const val ALICE_EMAIL = "alice.example@test.com"
        const val BOB_EMAIL = "bob.example@test.com"
        const val CHARLIE_EMAIL = "charlie@example@test.com"
    }

    object SentMessages {
        const val ALICE_SENT_MESSAGES = 3
        const val BOB_SENT_MESSAGES = 2
        const val CHARLIE_SENT_MESSAGES = 1
        const val TOTAL_AMOUNT =
            ALICE_SENT_MESSAGES
                .plus(BOB_SENT_MESSAGES)
                .plus(CHARLIE_SENT_MESSAGES)
    }

    val processedEntries =
        listOf(
            ProcessedEntryEntity(UUID.randomUUID(), "f1", Emails.ALICE_EMAIL),
            ProcessedEntryEntity(UUID.randomUUID(), "f2", Emails.ALICE_EMAIL),
            ProcessedEntryEntity(UUID.randomUUID(), "f3", Emails.ALICE_EMAIL),
            ProcessedEntryEntity(UUID.randomUUID(), "f4", Emails.BOB_EMAIL),
            ProcessedEntryEntity(UUID.randomUUID(), "f5", Emails.BOB_EMAIL),
            ProcessedEntryEntity(UUID.randomUUID(), "f6", Emails.CHARLIE_EMAIL),
        )
}
