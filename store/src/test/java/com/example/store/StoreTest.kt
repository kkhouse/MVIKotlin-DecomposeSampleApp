package com.example.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.badoo.reaktive.single.singleOf
import com.example.model.BaseStats
import com.example.model.Pokemon
import com.example.model.PokemonType
import com.example.model.ProcessResult
import com.example.model.type.PokemonsEnumModel
import com.example.repository.PokemonRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test

class StoreTest {
    private val mockRepository = mockk<PokemonRepository>()
    private val provider = PokemonListStoreProvider(DefaultStoreFactory(), mockRepository)

    val dummyPokemonType = PokemonType(name = "Electric")

    val dummyBaseStats = BaseStats(
        attack = 50,
        defense = 40,
        hp = 60,
        specialattack = 90,
        specialdefense = 80,
        speed = 110
    )

    val dummyPokemon = Pokemon(
        key = PokemonsEnumModel.abra,
        species = "Pikachu",
        sprite = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
        types = listOf(dummyPokemonType),
        baseStats = dummyBaseStats,
        baseStatsTotal = dummyBaseStats.attack + dummyBaseStats.defense + dummyBaseStats.hp +
                dummyBaseStats.specialattack + dummyBaseStats.specialdefense + dummyBaseStats.speed,
        num = 0
    )


    @Test
    fun testExecuteBootStrapAction() {
        val dummyResponse = singleOf(
            ProcessResult.Success(
                listOf(dummyPokemon)
            )
        )
        every { mockRepository.getPokemonResult() } returns dummyResponse
        val store = provider.provide() // start boot strap action
        assertEquals(
            dummyPokemon,
            store.state.pokemonList.get()?.get(0)
        )
    }
}