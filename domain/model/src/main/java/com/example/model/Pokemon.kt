package com.example.model

import android.os.Parcelable
import com.example.model.type.PokemonsEnumModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Pokemon(
    /**
     * The key of the Pokémon as stored in the API
     */
    val key: PokemonsEnumModel,
    /**
     * The species name for a Pokémon
     */
    val species: String,
    /**
     * The sprite for a Pokémon. For most Pokémon this will be the animated gif, with some
     * exceptions that were older-gen exclusive
     */
    val sprite: String,
    /**
     * The types for a Pokémon
     */
    val types: List<PokemonType>,
    /**
     * Base stats for a Pokémon
     */
    val baseStats: BaseStats,
    /**
     * The total of all base stats for a Pokémon
     */
    val baseStatsTotal: Int,

    /*
    Dex number
     */
    val num: Int,

    val favorite: Boolean = false,

    val pokemonRgbColor: Int = 0xFFFFFF
) : Parcelable
@Parcelize
data class PokemonType(
    /**
     * The name of the typ
     */
    val name: String,
) : Parcelable

@Parcelize
data class BaseStats(
    /**
     * The base attack stat of a Pokémon
     */
    val attack: Int,
    /**
     * The base defense stat of a Pokémon
     */
    val defense: Int,
    /**
     * The base HP stat of a pokémon
     */
    val hp: Int,
    /**
     * The base special attack stat of a Pokémon
     */
    val specialattack: Int,
    /**
     * The base special defense stat of a Pokémon
     */
    val specialdefense: Int,
    /**
     * The base speed stat of a Pokémon
     */
    val speed: Int,
) : Parcelable

