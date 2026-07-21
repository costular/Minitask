package com.costular.atomtasks.data.database

import androidx.room.withTransaction
import javax.inject.Inject

class RoomTransactionRunner @Inject constructor(
    private val minitaskDatabase: MinitaskDatabase
) : TransactionRunner {
    override suspend fun <T> runAsTransaction(block: suspend () -> T): T =
        minitaskDatabase.withTransaction(block)
}
