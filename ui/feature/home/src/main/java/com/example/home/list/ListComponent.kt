package com.example.home.list

import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.badoo.reaktive.base.Consumer
import com.example.core.util.asValue
import com.example.model.Pokemon
import com.example.model.ProcessResult
import com.example.store.PokemonListStore


interface ListComponent {

    val models: Value<Model>

    fun onItemClicked(pokemon: Pokemon)

    fun onFavorite(pokemon: Pokemon)

    fun onFavoriteFilter(isFilter: Boolean)

    fun onSearchPokemon(value: String)

    fun onPokemonColorProvider(pokemon: Pokemon)

    data class Model(
        val items: ProcessResult<List<Pokemon>>,
    )

    sealed class Output {
        data class Selected(val pokemon: Pokemon): Output()
    }
}

class ListComponentImpl(
    componentContext: ComponentContext,
    private val store: PokemonListStore,
    private val output: Consumer<ListComponent.Output>
): ListComponent, ComponentContext by componentContext {

    private val keptStore =
        instanceKeeper.getStore { store }

    override val models: Value<ListComponent.Model> = keptStore.asValue().map(stateToModel)
    override fun onItemClicked(pokemon: Pokemon) {
        output.onNext(ListComponent.Output.Selected(pokemon))
    }

    override fun onFavorite(pokemon: Pokemon) {
        store.accept(PokemonListStore.Intent.LikeOrDisLikePokemon(pokemon))
    }

    override fun onFavoriteFilter(isFilter: Boolean) {
        store.accept(PokemonListStore.Intent.FilterFavoritePokemon(isFilter))
    }

    override fun onSearchPokemon(value: String) {
        store.accept(PokemonListStore.Intent.FilterSearchingPokemon(value))
    }

    override fun onPokemonColorProvider(pokemon: Pokemon) {
        store.accept(PokemonListStore.Intent.UpdatePokemonColorState(pokemon = pokemon))
    }
}

internal val stateToModel: (PokemonListStore.State) -> ListComponent.Model =
    {
        ListComponent.Model(
            items = it.pokemonList,
        )
    }