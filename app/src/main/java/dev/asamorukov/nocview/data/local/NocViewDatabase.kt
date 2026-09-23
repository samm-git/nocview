package dev.asamorukov.nocview.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HostEntity::class, ServiceEntity::class, MetaEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class NocViewDatabase : RoomDatabase() {
    abstract fun statusDao(): StatusDao
}
