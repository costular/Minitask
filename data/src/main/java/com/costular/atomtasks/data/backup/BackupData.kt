package com.costular.atomtasks.data.backup

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
data class BackupDto(
    val tasks: List<TaskBackupDto>,
    val reminders: List<ReminderBackupDto>,
    val version: Int = 1
)

@Serializable
data class TaskBackupDto(
    val id: Long,
    @Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDate,
    val name: String,
    @Serializable(with = LocalDateSerializer::class)
    val day: LocalDate,
    val isDone: Boolean,
    val position: Int,
    val isRecurring: Boolean,
    val recurrenceType: String?,
    @Serializable(with = LocalDateSerializer::class)
    val recurrenceEndDate: LocalDate?,
    val parentId: Long?,
)

@Serializable
data class ReminderBackupDto(
    val reminderId: Long,
    @Serializable(with = LocalTimeSerializer::class)
    val time: LocalTime,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val taskId: Long,
)

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_DATE)
    }
}

object LocalTimeSerializer : KSerializer<LocalTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_LOCAL_TIME))
    }

    override fun deserialize(decoder: Decoder): LocalTime {
        return LocalTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_TIME)
    }
}
