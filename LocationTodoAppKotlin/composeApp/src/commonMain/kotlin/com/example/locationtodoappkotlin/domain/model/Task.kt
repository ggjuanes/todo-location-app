package com.example.locationtodoappkotlin.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

enum class TaskStatus {
    PENDING,
    COMPLETED
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val status: TaskStatus = TaskStatus.PENDING,
    val createdAt: Instant,
    val updatedAt: Instant
)
