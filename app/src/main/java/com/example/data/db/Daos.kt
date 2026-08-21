package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, escalationTier ASC, name ASC")
    fun getAllContacts(): Flow<List<EmergencyContact>>

    @Query("SELECT * FROM emergency_contacts WHERE isPrimary = 1 LIMIT 1")
    suspend fun getPrimaryContact(): EmergencyContact?

    @Query("SELECT * FROM emergency_contacts WHERE escalationTier = :tier")
    suspend fun getContactsForTier(tier: Int): List<EmergencyContact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact): Long

    @Update
    suspend fun updateContact(contact: EmergencyContact)

    @Delete
    suspend fun deleteContact(contact: EmergencyContact)
}

@Dao
interface UserAccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAccount(account: UserAccount): Long

    @Query("SELECT * FROM user_accounts WHERE loginId = :loginId LIMIT 1")
    suspend fun findByLoginId(loginId: String): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserAccount?

    @Query("SELECT * FROM user_accounts ORDER BY lastLoginAt DESC")
    fun getAllAccounts(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts WHERE id = :accountId LIMIT 1")
    suspend fun getAccountById(accountId: Long): UserAccount?
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alert_logs ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertLog>>

    @Query("SELECT * FROM alert_logs WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getAlertsForAccount(accountId: Long): Flow<List<AlertLog>>

    @Query("SELECT * FROM alert_logs WHERE status = 'QUEUED_OFFLINE' ORDER BY timestamp ASC")
    suspend fun getQueuedAlerts(): List<AlertLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertLog): Long

    @Update
    suspend fun updateAlert(alert: AlertLog)

    @Query("DELETE FROM alert_logs")
    suspend fun clearAllAlerts()

    @Query("DELETE FROM alert_logs WHERE accountId = :accountId")
    suspend fun clearAlertsForAccount(accountId: Long)
}
