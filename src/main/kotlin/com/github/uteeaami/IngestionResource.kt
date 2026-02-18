package com.github.uteeaami

import com.github.uteeaami.model.Sender
import com.github.uteeaami.model.Status
import com.github.uteeaami.service.IngestionService
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/ingestion")
class IngestionResource(
    val ingestionService: IngestionService,
) {
    @POST
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    fun start(): Map<String, String> {
        ingestionService.startIngestion()
        return mapOf("status" to "started")
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    fun status(): Status = ingestionService.currentStatus()

    @GET
    @Path("/top-senders")
    @Produces(MediaType.APPLICATION_JSON)
    fun topSenders(): List<Sender> = ingestionService.topSenders()

    /**
     *
     * Endpoint for resetting database state back to default,
     * used for testing / experimenting...
     *
     */
    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    fun reset(): Map<String, String> {
        ingestionService.reset()
        return mapOf("status" to "reset")
    }
}
