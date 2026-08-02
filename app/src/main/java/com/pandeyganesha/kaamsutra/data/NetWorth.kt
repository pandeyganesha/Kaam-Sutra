package com.pandeyganesha.kaamsutra.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.UUID


@Entity(tableName = "net_worth")
data class NetWorth(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val netWorth: Int
)
@Dao
interface NetWorthDao {
    @Insert
    suspend fun saveNetWorth(netWorth: NetWorth)

    @Query("Select * from net_worth WHERE id = 0 LIMIT 1")
    fun getNetWorth(): Flow<NetWorth?>
}
