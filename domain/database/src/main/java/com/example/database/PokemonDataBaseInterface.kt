package com.example.database

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.observable.Observable


interface PokemonDataBaseInterface {
    fun insertPokemon(pokemon: com.example.model.Pokemon)

    fun updatePokemonFavorite(pokemon: com.example.model.Pokemon, favorite: Boolean)

    fun updatePokemonColor(pokemon: com.example.model.Pokemon)

    fun selectAllPokemon(): Observable<List<Pokemon>>
    fun selectBaseStatsForPokemon(key: String):  SelectBaseStatsForPokemon
    fun selectTypesForPokemon(key: String): List<String>
    fun deletePokemon(key: String): Completable
    fun deleteAll(): Completable
}