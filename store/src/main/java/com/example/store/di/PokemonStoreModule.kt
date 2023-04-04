package com.example.store.di

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.example.repository.PokemonRepository
import com.example.store.PokemonListStore
import com.example.store.PokemonListStoreProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
class PokemonStoreModule {

    @Provides
    internal fun providePokemonListStore(
        pokemonRepository: PokemonRepository
    ): PokemonListStore {
        return PokemonListStoreProvider(
            storeFactory = DefaultStoreFactory(),
            pokemonRepository = pokemonRepository
        ).provide()
    }
}