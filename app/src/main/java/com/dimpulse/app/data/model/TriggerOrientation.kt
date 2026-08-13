package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TriggerOrientation(
    val title: String,
    val description: String
) {
    ANY_ORIENTATION(
        title = "Any Orientation",
        description = "Flash regardless of how the phone is positioned"
    ),
    ONLY_FACE_DOWN(
        title = "Only Face-Down on Table (HiLight)",
        description = "Flash strictly when phone is placed flat face-down on a desk"
    ),
    EXCEPT_IN_POCKET(
        title = "Smart Desk & Pocket Mode",
        description = "Allow on table (face-down or face-up), suppress inside pocket"
    )
}
