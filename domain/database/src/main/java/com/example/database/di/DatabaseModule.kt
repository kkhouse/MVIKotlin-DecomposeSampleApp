package com.example.database.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.database.Database
import com.example.database.DefaultDataBase
import com.example.database.PokemonDataBaseInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ActivityRetainedComponent::class)
class DatabaseModule {

    @Provides
    fun provideSqlDriver(
        @ApplicationContext context: Context
    ): SqlDriver {
        return AndroidSqliteDriver(Database.Schema, context, "test.db")
    }

    @Provides
    fun providePokemonDataBase(driver: SqlDriver): PokemonDataBaseInterface {
        return DefaultDataBase(driver)
    }
}