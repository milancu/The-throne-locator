package cz.cvut.fel.thethronelocator.model

import java.time.LocalDateTime

data class Reminder(
    val whenTime: LocalDateTime,
    val user: User,
    val toilet: Toilet
)