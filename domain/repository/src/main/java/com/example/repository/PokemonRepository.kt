package com.example.repository

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.single.Single
import com.example.model.Pokemon
import com.example.model.ProcessResult

interface PokemonRepository {
    fun observeAllPokemon(): Observable<ProcessResult<List<Pokemon>>>
    fun insertPokemon(pokemon: Pokemon)
    fun updateFavorite(pokemon: Pokemon, favorite: Boolean)
//    fun deletePokemon(pokemon: Pokemon): Completable
//    fun deleteAllPokemon(): Completable

    fun updatePokemonColor(pokemon: Pokemon)

    fun getPokemonResult(): Single<ProcessResult<List<Pokemon>>>
}