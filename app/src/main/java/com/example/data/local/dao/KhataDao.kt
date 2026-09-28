package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.KhataTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KhataDao {
    @Query("SELECT * FROM khata_transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<KhataTransactionEntity>>

    @Query("SELECT * FROM khata_transactions WHERE partyId = :partyId ORDER BY date DESC, id DESC")
    fun getTransactionsForParty(partyId: Long): Flow<List<KhataTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: KhataTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<KhataTransactionEntity>)

    @Query("DELETE FROM khata_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("SELECT COUNT(*) FROM khata_transactions")
    suspend fun getCount(): Int
}
