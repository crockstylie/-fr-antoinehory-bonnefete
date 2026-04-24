package fr.antoinehory.bonnefete.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.antoinehory.bonnefete.data.local.AppDatabase
import fr.antoinehory.bonnefete.data.local.dao.SaintDao
import javax.inject.Singleton

/**
 * Hilt module for local database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bonne_fete_db"
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }

    @Provides
    fun provideSaintDao(database: AppDatabase): SaintDao {
        return database.saintDao()
    }
}
