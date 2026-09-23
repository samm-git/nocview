package dev.asamorukov.nocview.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusDao {

    @Query("SELECT * FROM hosts ORDER BY name COLLATE NOCASE")
    fun observeHosts(): Flow<List<HostEntity>>

    @Query("SELECT * FROM hosts WHERE name = :name LIMIT 1")
    fun observeHost(name: String): Flow<HostEntity?>

    @Query("SELECT * FROM services ORDER BY hostName COLLATE NOCASE, description COLLATE NOCASE")
    fun observeServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE hostName = :hostName ORDER BY description COLLATE NOCASE")
    fun observeServicesForHost(hostName: String): Flow<List<ServiceEntity>>

    @Query(
        "SELECT * FROM services WHERE hostName = :hostName AND description = :description LIMIT 1",
    )
    fun observeService(hostName: String, description: String): Flow<ServiceEntity?>

    @Query("SELECT * FROM meta WHERE id = 0")
    fun observeMeta(): Flow<MetaEntity?>

    @Query("SELECT * FROM meta WHERE id = 0")
    suspend fun metaNow(): MetaEntity?

    @Upsert
    suspend fun upsertHosts(hosts: List<HostEntity>)

    @Upsert
    suspend fun upsertServices(services: List<ServiceEntity>)

    @Upsert
    suspend fun upsertMeta(meta: MetaEntity)

    @Query("DELETE FROM hosts")
    suspend fun clearHosts()

    @Query("DELETE FROM services")
    suspend fun clearServices()
}
