package com.github.uteeaami.config

import io.smallrye.config.ConfigMapping
import io.smallrye.config.WithName
import java.util.UUID

const val CONFIG_ROOT = "enron-ingestion-service"

@ConfigMapping(prefix = CONFIG_ROOT)
interface IngestionConfig {
    @get:WithName("datasource")
    val dataSource: DataSource

    @get:WithName("ingestion-job")
    val ingestionJob: IngestionJob

    interface IngestionJob {
        @WithName("id")
        fun id(): UUID

        @WithName("timeout-seconds")
        fun timeoutSeconds(): Long
    }

    interface DataSource {
        @WithName("path")
        fun path(): String
    }
}
