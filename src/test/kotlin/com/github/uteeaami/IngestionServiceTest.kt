package com.github.uteeaami

import com.github.uteeaami.TestConstants.Emails
import com.github.uteeaami.repository.ProcessedEntryRepository
import com.github.uteeaami.service.IngestionService
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IngestionServiceTest {
    @Inject
    lateinit var service: IngestionService

    @Inject
    lateinit var processedEntryRepository: ProcessedEntryRepository

    @Inject
    private lateinit var flyway: Flyway

    @BeforeEach
    fun setup() {
        flyway.clean()
        flyway.migrate()
        processedEntryRepository.persistEntries(TestConstants.processedEntries)
    }

    @Test
    fun `should only persist new entries`() {
        val userDirectory = "dir1"

        val expectedEntriesAmount = 8

        val inputEntries =
            mapOf(
                "f1" to Emails.ALICE_EMAIL, // OLD
                "f2" to Emails.ALICE_EMAIL, // OLD
                "f7" to Emails.BOB_EMAIL, // NEW
                "f8" to Emails.CHARLIE_EMAIL, // NEW
            )

        service.processUserDirectory(userDirectory, inputEntries)

        val actualEntriesAmount = processedEntryRepository.getTotalProcessedEntries()
        assertEquals(expectedEntriesAmount, actualEntriesAmount.toInt())
    }
}
