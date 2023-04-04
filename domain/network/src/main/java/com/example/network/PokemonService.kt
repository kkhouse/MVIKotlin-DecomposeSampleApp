package com.example.network

import com.badoo.reaktive.single.Single
import com.example.model.ProcessResult

interface PokemonService {
    fun getAllPokemon(): Single<ProcessResult<List<GetPokemonQuery.GetAllPokemon>>>
}