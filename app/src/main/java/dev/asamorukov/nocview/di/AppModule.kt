package dev.asamorukov.nocview.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.asamorukov.nocview.data.local.NocViewDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NocViewDatabase =
        Room.databaseBuilder(context, NocViewDatabase::class.java, "nocview.db")
            .fallbackToDestructiveMigration()
            .build()
}
