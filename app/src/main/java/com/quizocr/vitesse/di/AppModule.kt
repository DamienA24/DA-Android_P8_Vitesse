package com.quizocr.vitesse.di

import android.content.Context
import com.quizocr.vitesse.data.dao.CandidateDao
import com.quizocr.vitesse.data.database.AppDatabase
import com.quizocr.vitesse.data.repository.CandidateRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)


    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context, coroutineScope: CoroutineScope): AppDatabase {
        return AppDatabase.getDatabase(context, coroutineScope)
    }


    @Provides
    fun provideCandidatesDao(appDatabase: AppDatabase): CandidateDao {
        return appDatabase.candidateDao()
    }

    @Provides
    @Singleton
    fun provideCandidateRepository(candidateDao: CandidateDao): CandidateRepository {
        return CandidateRepository(candidateDao)
    }
}