package com.github.uteeaami.exception

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class IngestionInProgressExceptionMapper : ExceptionMapper<IngestionInProgressException> {
    override fun toResponse(exception: IngestionInProgressException): Response =
        Response
            .status(Response.Status.CONFLICT)
            .entity(mapOf("error" to exception.message))
            .build()
}
