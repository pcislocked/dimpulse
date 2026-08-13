package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class AlertImportanceFilter(
    val title: String,
    val description: String
) {
    ONLY_ALERTING(
        title = "Only Loud / Alerting (Default)",
        description = "Flash only for notifications configured with sound/vibration (ignores silent background spam)"
    ),
    ALL_INCLUDING_SILENT(
        title = "All, Including Silent",
        description = "Flash for all notifications, including low-priority silent ones"
    ),
    NONE(
        title = "None (Muted)",
        description = "Never flash for notifications"
    )
}
