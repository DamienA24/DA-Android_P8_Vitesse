package com.quizocr.vitesse.di

import com.quizocr.vitesse.data.repository.CurrencyConversionRepository
import com.quizocr.vitesse.data.repository.service.ExchangeRateApi
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCurrencyConversionRepository(
        currencyConversionRepository: CurrencyConversionRepository
    ): ExchangeRateApi
}