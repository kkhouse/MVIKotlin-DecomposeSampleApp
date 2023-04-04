package com.example.store

import androidx.compose.ui.node.IntermediateLayoutModifierNode
import com.arkivanov.mvikotlin.core.store.Store
import com.example.model.Pokemon
import com.example.model.ProcessResult
import com.example.store.PokemonListStore.*

interface PokemonListStore : Store<Intent, State, Label> {
    /*
    UI Event をStore上で解釈したもの
    ビジネスロジックで何をしたいかを表す
     */
    sealed class Intent {
        data class LikeOrDisLikePokemon(val pokemon: Pokemon): Intent()

        data class FilterFavoritePokemon(val isFilter: Boolean): Intent()

        data class FilterSearchingPokemon(val searchValue: String): Intent()

        data class UpdatePokemonColorState(val pokemon: Pokemon): Intent()
    }

    /*
    データの状態
     */
    data class State(
        val pokemonList: ProcessResult<List<Pokemon>> = ProcessResult.Loading,
        val favoriteState: FavoriteState = FavoriteState()
    )
    /*
    お気に入りボタンのState
     */
    data class FavoriteState(
        val whichPokemonKey: String? = null,
        val state: ProcessResult<Unit> = ProcessResult.Success(Unit)
    )

    /*
    SideEffectの目標
     */
    sealed class Label {
        data class AnyLabel(val any : String): Label()
    }
}