package com.example.store

import android.util.Log
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.ReaktiveExecutor
import com.badoo.reaktive.completable.doOnAfterComplete
import com.badoo.reaktive.completable.doOnBeforeComplete
import com.badoo.reaktive.completable.doOnBeforeSubscribe
import com.badoo.reaktive.observable.doOnBeforeNext
import com.badoo.reaktive.observable.flatMap
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.observable
import com.badoo.reaktive.single.asObservable
import com.example.model.Pokemon
import com.example.model.ProcessResult
import com.example.model.utils.sortPokemon
import com.example.repository.PokemonRepository
import com.example.store.PokemonListStore.*
import javax.inject.Inject

internal class PokemonListStoreProvider @Inject constructor(
    private val storeFactory: StoreFactory,
    private val pokemonRepository: PokemonRepository,
    private val pokemonListCache: MutableList<Pokemon> = mutableListOf()
) {

    fun provide(): PokemonListStore =
        object : PokemonListStore, Store<Intent,State, Label> by storeFactory.create(
            name = "PokemonListStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Action.BootStrap),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    /*
    ReducerへのMessage
     */
    private sealed class Msg {
        data class ItemsLoaded(val items: ProcessResult<List<Pokemon>>): Msg()

        data class ItemsFiltered(val filteredList: List<Pokemon>): Msg()
    }

    /*
    Actionは主にBootStrap（Store初期化）時のイベント
     */
    private sealed class Action {
        object BootStrap: Action() // init store event
    }

    /*
    IntentとActionをMsgにマッピングしてdispatchするところ
     */
    private inner class ExecutorImpl : ReaktiveExecutor<Intent, Action, State, Msg, Label>() {

        /*
        Store初期化時の引数である以下実行時に開始されるFunction
            `bootstrapper = SimpleBootstrapper(Action.BootStrap)`
         */
        override fun executeAction(action: Action, getState: () -> State) {
            when(action) {
                Action.BootStrap ->  {
                    pokemonRepository
                        .observeAllPokemon()
                        .map(::sortPokemon)
                        .map { result -> result.tap { pokemonListCache.addAll(it) } }
                        .map(Msg::ItemsLoaded) // Msgにマッピングして、Reducerへdispatch
                        .subscribeScoped(onNext = ::dispatch)
                }
            }
        }

        override fun executeIntent(intent: Intent, getState: () -> State) {
            when(intent) {
                is Intent.LikeOrDisLikePokemon -> likeOrDisLikePokemon(intent.pokemon)
                is Intent.FilterFavoritePokemon -> filterPokemon(isFavoriteFilter = intent.isFilter)
                is Intent.FilterSearchingPokemon -> filterPokemon(filterValue = intent.searchValue)
                is Intent.UpdatePokemonColorState -> {
                    pokemonRepository.updatePokemonColor(pokemon = intent.pokemon)
                }
            }
        }

        private fun filterPokemon(filterValue: String) {
            return dispatch(
                Msg.ItemsFiltered(
                    pokemonListCache.filter {
                        when(filterValue.isEmpty()) {
                            true -> true
                            else -> it.species.startsWith(filterValue)
                        }
                    }
                )
            )
        }

        private fun filterPokemon(isFavoriteFilter: Boolean) {
            return dispatch(
                Msg.ItemsFiltered(
                    pokemonListCache.filter {
                        when(isFavoriteFilter) {
                            true -> it.favorite
                            else -> true
                        }
                    }
                )
            )
        }

        private fun likeOrDisLikePokemon(pokemon: Pokemon) {
            (!pokemon.favorite).let { newFavorite ->
                pokemonRepository.updateFavorite(pokemon, newFavorite)
            }
        }
    }

    /*
    Executorから渡されるMsgを新しいStateに変換するところ
     */
    private object ReducerImpl : Reducer<State, Msg> {

        override fun State.reduce(msg: Msg): State =
            when(msg) {
                is Msg.ItemsFiltered -> copy(pokemonList = ProcessResult.Success(msg.filteredList))
                is Msg.ItemsLoaded -> copy(pokemonList = msg.items)
            }
    }
}