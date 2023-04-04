package com.example.database

import android.util.Log
import androidx.core.graphics.toColorLong
import app.cash.sqldelight.Query
import app.cash.sqldelight.db.SqlDriver
import com.badoo.reaktive.base.setCancellable
import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.observable.*
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.single.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log
import com.example.model.Pokemon as PokemonModel

@Singleton
class DefaultDataBase @Inject constructor(
    driver: SqlDriver
): PokemonDataBaseInterface {
    private val singleQueries: Single<PokemonQueries> =
        singleOf(driver)
            .map { Database(it).pokemonQueries }
            .asObservable()
            .replay()
            .autoConnect()
            .firstOrError()

    private val queries = Database(driver).pokemonQueries

    override fun insertPokemon(pokemon: PokemonModel) {
        queries.insertPokemon(
            key = pokemon.key.name,
            species = pokemon.species,
            sprite = pokemon.sprite,
            baseStatsTotal = pokemon.baseStatsTotal.toLong(),
            num = pokemon.num.toLong(),
            favorite = 0,
            color = null
        )
        pokemon.types.forEach { type ->
            queries.insertPokemonType(
                key = pokemon.key.name,
                type = type.name
            )
        }
        pokemon.baseStats.let { baseStats ->
            queries.insertPokemonBaseStat(
                key = pokemon.key.name,
                attack = baseStats.attack.toLong(),
                defense = baseStats.defense.toLong(),
                hp = baseStats.hp.toLong(),
                specialattack = baseStats.specialattack.toLong(),
                specialdefense = baseStats.specialdefense.toLong(),
                speed = baseStats.speed.toLong()
            )
        }
    }

    override fun updatePokemonFavorite(pokemon: com.example.model.Pokemon, favorite: Boolean) {
        queries.updatePokemonFavorite(
            key = pokemon.key.name,
            favorite =  if (favorite) 1 else 0
        )
    }

    override fun updatePokemonColor(pokemon: com.example.model.Pokemon) {
       queries.updatePokemonColor(key = pokemon.key.name, color = pokemon.pokemonRgbColor.toLong())
    }

    override fun selectAllPokemon(): Observable<List<Pokemon>> =
        query(PokemonQueries::selectAllPokemon)
            .observe { it.executeAsList() }

    // TODO try- catch
    override fun selectBaseStatsForPokemon(key: String): SelectBaseStatsForPokemon {
        return queries.selectBaseStatsForPokemon(key).executeAsOne()
    }

    override fun selectTypesForPokemon(key: String): List<String> {
        return queries.selectTypesForPokemon(key).executeAsList()
    }

    override fun deletePokemon(key: String): Completable {
        return execute { deletePokemon(key) }
    }

    override fun deleteAll(): Completable {
        return execute { deleteAll() }
    }


    private fun <T: Any> query(query : (PokemonQueries) -> Query<T>) : Single<Query<T>> =
        singleQueries
            .observeOn(ioScheduler)
            .map(query)

    private fun execute(query: (PokemonQueries) -> Unit): Completable =
        singleQueries
            .observeOn(ioScheduler)
            .doOnBeforeSuccess(query)
            .asCompletable()

    private fun <T : Any, R> Single<Query<T>>.observe(get: (Query<T>) -> R): Observable<R> =
        flatMapObservable { it.observed() }
            .observeOn(ioScheduler)
            .map(get)

    private fun <T : Any> Query<T>.observed(): Observable<Query<T>> =
        observable { emitter ->
            val listener = Query.Listener { emitter.onNext(this@observed) }

            emitter.onNext(this@observed)
            addListener(listener)
            emitter.setCancellable { removeListener(listener) }
        }
}