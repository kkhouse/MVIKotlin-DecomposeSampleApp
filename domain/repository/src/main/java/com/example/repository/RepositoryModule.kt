package com.example.repository

import com.example.database.PokemonDataBaseInterface
import com.example.network.PokemonService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
class RepositoryModule {

    @Provides
    internal fun providePokemonRepository(
        db: PokemonDataBaseInterface,
        api: PokemonService,
    ): PokemonRepository {
        return PokemonRepositoryImpl(
            db = db,
            api = api,
        )
    }
}