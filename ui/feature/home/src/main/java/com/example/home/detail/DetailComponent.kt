package com.example.home.detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.badoo.reaktive.base.Consumer
import com.example.model.Pokemon

interface DetailComponent {

    val models: Value<Model>

    fun onCloseClicked()

    data class Model(
        val pokemon: Pokemon,
    )

    sealed class Output {
        object List: Output()
    }
}

class DetailComponentImpl(
    componentContext: ComponentContext,
    private val pokemon: Pokemon,
    private val output: Consumer<DetailComponent.Output>
): DetailComponent, ComponentContext by componentContext {

    override val models: Value<DetailComponent.Model> = MutableValue(DetailComponent.Model(pokemon))

    override fun onCloseClicked() {
        output.onNext(DetailComponent.Output.List)
    }

}