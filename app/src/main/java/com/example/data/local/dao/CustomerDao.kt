package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllParties(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE type = :type ORDER BY currentBalance DESC, name ASC")
    fun getPartiesByType(type: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getPartyById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE currentBalance > 0 AND type = 'CUSTOMER' ORDER BY currentBalance DESC")
    fun getDebtors(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Update
    suspend fun updateParty(customer: CustomerEntity)

    @Query("UPDATE customers SET currentBalance = currentBalance + :delta WHERE id = :id")
    suspend fun updateBalance(id: Long, delta: Double)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteParty(id: Long)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCount(): Int
}
