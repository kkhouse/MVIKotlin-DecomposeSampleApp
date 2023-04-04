package com.example.repository

import android.util.Log
import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completable
import com.badoo.reaktive.completable.subscribeOn
import com.badoo.reaktive.observable.*
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.scheduler.mainScheduler
import com.badoo.reaktive.single.*
import com.example.database.PokemonDataBaseInterface
import com.example.model.BaseStats
import com.example.model.Pokemon
import com.example.model.PokemonType
import com.example.model.ProcessResult
import com.example.model.type.PokemonsEnumModel
import com.example.network.PokemonService
import javax.inject.Inject
import kotlin.math.log

/*
map to domain model from data( api ) model
 */
class PokemonRepositoryImpl @Inject constructor(
    private val db : PokemonDataBaseInterface,
    private val api: PokemonService,
): PokemonRepository {

    override fun observeAllPokemon(): Observable<ProcessResult<List<Pokemon>>> {
        return getAllPokemonFromDatabase()
            .flatMap { processResult ->
                // データベースが空だったらAPIから取得する
                when(processResult) {
                    is ProcessResult.Success -> when(processResult.data.isEmpty()){
                        true -> getPokemonResult().asObservable()
                                .doOnAfterNext(::saveDatabaseOnProcessResult)
                        else -> observableOf(processResult)
                    }
                    else -> observableOf(processResult)
                }
            }
            .observeOn(mainScheduler)
    }

    /*
    TODO suspend
     */
    override fun updateFavorite(pokemon: Pokemon, favorite: Boolean){
        db.updatePokemonFavorite(pokemon, favorite = favorite)
    }

    /*
    TODO suspend
     */
    override fun updatePokemonColor(pokemon: Pokemon) {
        db.updatePokemonColor(pokemon = pokemon)
    }

    /*
    TODO suspend
     */
    override fun insertPokemon(pokemon: Pokemon){
        db.insertPokemon(pokemon)
    }

    override fun getPokemonResult(): Single<ProcessResult<List<Pokemon>>> {
        return getPokemonResultFromNetwork().observeOn(mainScheduler)
    }

    private fun getPokemonResultFromNetwork() : Single<ProcessResult<List<Pokemon>>>  {
        return api.getAllPokemon().map { result ->
            result.map { pokemons ->
                pokemons.map { pokemon ->
                    Pokemon(
                        key = PokemonsEnumModel.safeValueOf(pokemon.key.rawValue),
                        species = pokemon.species,
                        sprite = pokemon.sprite,
                        types = pokemon.types.map { PokemonType(it.name) },
                        baseStats = BaseStats(
                            attack = pokemon.baseStats.attack,
                            defense = pokemon.baseStats.defense,
                            hp = pokemon.baseStats.hp,
                            specialattack = pokemon.baseStats.specialattack,
                            specialdefense = pokemon.baseStats.specialdefense,
                            speed = pokemon.baseStats.speed
                        ),
                        baseStatsTotal = pokemon.baseStatsTotal,
                        num = pokemon.num,
                    )
                }
            }
        }
    }

    private fun getAllPokemonFromDatabase(): Observable<ProcessResult<List<Pokemon>>> =
        db.selectAllPokemon().map { pokemons ->
            pokemons.map { pokemon ->
                db.selectBaseStatsForPokemon(pokemon.key).let { stats ->
                    Pokemon(
                        key = PokemonsEnumModel.safeValueOf(pokemon.key),
                        species = pokemon.species,
                        sprite = pokemon.sprite,
                        types = db.selectTypesForPokemon(pokemon.key).map { PokemonType(it) },
                        baseStats = BaseStats(
                            attack = stats.attack.toInt(),
                            defense = stats.defense.toInt(),
                            hp = stats.hp.toInt(),
                            specialattack = stats.specialattack.toInt(),
                            specialdefense = stats.specialdefense.toInt(),
                            speed = stats.speed.toInt()
                        ),
                        baseStatsTotal = pokemon.baseStatsTotal.toInt(),
                        num = pokemon.num.toInt(),
                        favorite = pokemon.favorite.toInt() == 1,
                        pokemonRgbColor = pokemon.color?.toInt() ?: 0xFFFFFF
                    )
                }
            }.asProcessResult()
        }.defaultIfEmpty(ProcessResult.Loading)

    private fun List<Pokemon>.asProcessResult() : ProcessResult<List<Pokemon>> {
        return ProcessResult.Success(this)
    }

    private fun saveDatabaseOnProcessResult(result: ProcessResult<List<Pokemon>>) {
        if (result is ProcessResult.Success) {
            result.data.forEach {
                db.insertPokemon(it)
            }
        }
    }
}