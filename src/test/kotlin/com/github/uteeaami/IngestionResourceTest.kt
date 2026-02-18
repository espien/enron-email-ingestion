package com.github.uteeaami

import com.github.uteeaami.TestConstants.Emails.ALICE_EMAIL
import com.github.uteeaami.TestConstants.Emails.BOB_EMAIL
import com.github.uteeaami.TestConstants.Emails.CHARLIE_EMAIL
import com.github.uteeaami.TestConstants.SentMessages.ALICE_SENT_MESSAGES
import com.github.uteeaami.TestConstants.SentMessages.BOB_SENT_MESSAGES
import com.github.uteeaami.TestConstants.SentMessages.CHARLIE_SENT_MESSAGES
import com.github.uteeaami.TestConstants.SentMessages.TOTAL_AMOUNT
import com.github.uteeaami.entity.IngestionStatusEntity
import com.github.uteeaami.model.IngestionStatus
import com.github.uteeaami.repository.IngestionJobRepository
import com.github.uteeaami.repository.ProcessedEntryRepository
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import jakarta.inject.Inject
import org.flywaydb.core.Flyway
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IngestionResourceTest {
    @Inject
    lateinit var processedEntryRepository: ProcessedEntryRepository

    @Inject
    lateinit var ingestionJobRepository: IngestionJobRepository

    @Inject
    private lateinit var flyway: Flyway

    private companion object {
        const val ROOT_PATH = "/ingestion"
    }

    @BeforeEach
    fun setup() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun `should returns 200 when ingestion is started`() {
        RestAssured
            .given()
            .`when`()
            .post("$ROOT_PATH/start")
            .then()
            .statusCode(200)
            .body("status", equalTo("started"))
    }

    @Test
    fun `should return 409 if ingestion is already in progress`() {
        ingestionJobRepository.markAs(IngestionStatusEntity.IN_PROGRESS)

        RestAssured
            .given()
            .`when`()
            .post("$ROOT_PATH/start")
            .then()
            .statusCode(409)
            .body("error", equalTo("Ingestion already started"))
    }

    @Test
    fun `should return total messagesProcessed when entries exist`() {
        processedEntryRepository.persistEntries(TestConstants.processedEntries)

        RestAssured
            .given()
            .`when`()
            .get("$ROOT_PATH/status")
            .then()
            .statusCode(200)
            .body("status", equalTo(IngestionStatus.IDLE.name))
            .body("messagesProcessed", equalTo(TOTAL_AMOUNT))
    }

    @Test
    fun `should return zero messagesProcessed when no entries exist`() {
        RestAssured
            .given()
            .`when`()
            .get("$ROOT_PATH/status")
            .then()
            .statusCode(200)
            .body("status", equalTo(IngestionStatus.IDLE.name))
            .body("messagesProcessed", equalTo(0))
    }

    @Test
    fun `should return top senders when entries exist`() {
        processedEntryRepository.persistEntries(TestConstants.processedEntries)

        RestAssured
            .given()
            .`when`()
            .get("$ROOT_PATH/top-senders")
            .then()
            .statusCode(200)
            .body("[0].email", equalTo(ALICE_EMAIL))
            .body("[0].sentMessages", equalTo(ALICE_SENT_MESSAGES))
            .body("[1].email", equalTo(BOB_EMAIL))
            .body("[1].sentMessages", equalTo(BOB_SENT_MESSAGES))
            .body("[2].email", equalTo(CHARLIE_EMAIL))
            .body("[2].sentMessages", equalTo(CHARLIE_SENT_MESSAGES))
    }

    @Test
    fun `should return empty list when no entries exist`() {
        RestAssured
            .given()
            .`when`()
            .get("$ROOT_PATH/top-senders")
            .then()
            .statusCode(200)
            .body(equalTo("[]"))
    }
}
