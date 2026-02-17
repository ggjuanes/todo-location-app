package com.example.locationtodoappkotlin.data.local

import androidx.room.TypeConverter
import com.example.locationtodoappkotlin.domain.model.TaskStatus
import kotlin.time.Instant

class Converters {
    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(value)

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)
}
