package com.example.model.utils

import com.example.model.BaseStats
import com.example.model.Pokemon
import com.example.model.PokemonType
import com.example.model.ProcessResult
import com.example.model.type.PokemonsEnumModel

fun sortPokemon(result: ProcessResult<List<Pokemon>>): ProcessResult<List<Pokemon>> {
    return result.map { list ->
        val minusList = list.filter { it.num < 0 }.sortedByDescending { it.num }.reversed()
        val plusList = list.filter { it.num >= 0 }.sortedBy { it.num }
        plusList + minusList
    }
}


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
    types = listOf(dummyPokemonType, dummyPokemonType),
    baseStats = dummyBaseStats,
    baseStatsTotal = dummyBaseStats.attack + dummyBaseStats.defense + dummyBaseStats.hp +
            dummyBaseStats.specialattack + dummyBaseStats.specialdefense + dummyBaseStats.speed,
    num = 1
)