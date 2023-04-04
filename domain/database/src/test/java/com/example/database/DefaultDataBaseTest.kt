package com.example.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.badoo.reaktive.completable.subscribe
import com.example.database.Database.Companion.Schema
import com.example.model.BaseStats
import com.example.model.PokemonType
import com.example.model.type.PokemonsEnumModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import com.example.model.Pokemon as PokemonModel


internal class DefaultDataBaseTest {
    private lateinit var driver:  SqlDriver
    private lateinit var queries:  PokemonQueries
    val dummyPokemonType = PokemonType(name = "Electric")

    val dummyBaseStats = BaseStats(
        attack = 50,
        defense = 40,
        hp = 60,
        specialattack = 90,
        specialdefense = 80,
        speed = 110
    )

    val dummyPokemon = PokemonModel(
        key = PokemonsEnumModel.abra,
        species = "Pikachu",
        sprite = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
        types = listOf(dummyPokemonType),
        baseStats = dummyBaseStats,
        baseStatsTotal = dummyBaseStats.attack + dummyBaseStats.defense + dummyBaseStats.hp +
                dummyBaseStats.specialattack + dummyBaseStats.specialdefense + dummyBaseStats.speed,
        num = 1
    )

    @Before
    fun before() {
        driver = createTestDriver()
        queries = createTestQueries(driver)
    }

    private fun createTestDriver(): SqlDriver {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        Schema.create(driver)
        return driver
    }

    private fun createTestQueries(driver: SqlDriver): PokemonQueries {
        val database = Database(driver)
        return database.pokemonQueries
    }

    @Test
    fun test() {
        runBlocking {
            val database = DefaultDataBase(driver)
            database.insertPokemon(dummyPokemon).subscribe(
                isThreadLocal = true,
                onComplete = {
                    assertEquals(
                        database.selectAllPokemon(),
                        listOf(Pokemon(
                            dummyPokemon.key.name,
                            dummyPokemon.species,
                            dummyPokemon.sprite,
                            dummyPokemon.baseStatsTotal.toLong()
                        ))
                    )
                    assertEquals(
                        database.selectTypesForPokemon(dummyPokemon.key.rawValue),
                        dummyBaseStats
                    )
                    assertEquals(
                        database.selectTypesForPokemon(dummyPokemon.key.rawValue),
                        listOf(dummyPokemonType.name)
                    )

                    database.deletePokemon(dummyPokemon.key.rawValue)
                    assertEquals(
                        database.selectAllPokemon(),
                        listOf<Pokemon>()
                    )
                    try {
                        database.selectTypesForPokemon(dummyPokemon.key.rawValue)
                    } catch (e : Exception) {
                        assertTrue(e is NullPointerException)
                    }
                }
            )

        }
    }
}