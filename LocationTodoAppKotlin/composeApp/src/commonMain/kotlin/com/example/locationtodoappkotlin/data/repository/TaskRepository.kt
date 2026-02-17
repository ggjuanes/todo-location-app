package com.example.locationtodoappkotlin.data.repository

import com.example.locationtodoappkotlin.data.local.TaskDao
import com.example.locationtodoappkotlin.domain.model.Task
import com.example.locationtodoappkotlin.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: String): Task?
    suspend fun createTask(title: String, description: String, location: String): Result<Task>
    suspend fun updateTask(id: String, title: String, description: String, location: String): Result<Task>
    suspend fun deleteTask(id: String)
    suspend fun toggleTaskStatus(id: String)
}

@OptIn(ExperimentalTime::class)
private fun now() = Clock.System.now()

class TaskRepositoryImpl(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    override suspend fun getTaskById(id: String): Task? = taskDao.getTaskById(id)

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createTask(title: String, description: String, location: String): Result<Task> {
        val validationError = validate(title, description, location)
        if (validationError != null) return Result.failure(IllegalArgumentException(validationError))

        val currentTime = now()
        val task = Task(
            id = Uuid.random().toString(),
            title = title.trim(),
            description = description.trim(),
            location = location.trim(),
            status = TaskStatus.PENDING,
            createdAt = currentTime,
            updatedAt = currentTime
        )
        taskDao.insertTask(task)
        return Result.success(task)
    }

    override suspend fun updateTask(id: String, title: String, description: String, location: String): Result<Task> {
        val validationError = validate(title, description, location)
        if (validationError != null) return Result.failure(IllegalArgumentException(validationError))

        val existing = taskDao.getTaskById(id)
            ?: return Result.failure(IllegalArgumentException("Task not found"))

        val updated = existing.copy(
            title = title.trim(),
            description = description.trim(),
            location = location.trim(),
            updatedAt = now()
        )
        taskDao.updateTask(updated)
        return Result.success(updated)
    }

    override suspend fun deleteTask(id: String) {
        taskDao.deleteTask(id)
    }

    override suspend fun toggleTaskStatus(id: String) {
        val task = taskDao.getTaskById(id) ?: return
        val newStatus = when (task.status) {
            TaskStatus.PENDING -> TaskStatus.COMPLETED
            TaskStatus.COMPLETED -> TaskStatus.PENDING
        }
        taskDao.updateTask(task.copy(status = newStatus, updatedAt = now()))
    }

    private fun validate(title: String, description: String, location: String): String? {
        val trimmedTitle = title.trim()
        val trimmedDescription = description.trim()
        val trimmedLocation = location.trim()

        return when {
            trimmedTitle.isBlank() -> "Title is required"
            trimmedTitle.length > 100 -> "Title must be 100 characters or less"
            trimmedDescription.length > 500 -> "Description must be 500 characters or less"
            trimmedLocation.isBlank() -> "Location is required"
            else -> null
        }
    }
}
