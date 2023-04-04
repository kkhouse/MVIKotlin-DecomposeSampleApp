package com.example.home.root

import android.os.Parcelable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.badoo.reaktive.base.Consumer
import com.example.core.util.Consumer
import com.example.home.detail.DetailComponent
import com.example.home.list.ListComponent
import com.example.home.detail.DetailComponentImpl
import com.example.home.list.ListComponentImpl
import com.example.model.Pokemon
import com.example.store.PokemonListStore
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

interface MainRoot {
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        data class List(val component: ListComponent) : Child()
        data class Detail(val component: DetailComponent) : Child()
    }
}

class MainRootComponent(
    componentContext: ComponentContext,
    private val listComponent: (ComponentContext, Consumer<ListComponent.Output>) -> ListComponent,
    private val detailComponent: (ComponentContext, pokemon: Pokemon, Consumer<DetailComponent.Output>) -> DetailComponent
): MainRoot, ComponentContext by componentContext {

    @Inject constructor(
        componentContext: ComponentContext,
        store: PokemonListStore
    ) : this(
        componentContext = componentContext,
        listComponent = { childContext, output ->
            ListComponentImpl(
                componentContext = childContext,
                output = output,
                store = store
            )
        },
        detailComponent = { childContext, pokemon, output ->
            DetailComponentImpl(
                componentContext = childContext,
                pokemon = pokemon,
                output = output,
            )
        }
    )

    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            initialConfiguration = Configuration.Main,
            handleBackButton = true,
            childFactory = ::createChild
        )

    override val childStack: Value<ChildStack<*, MainRoot.Child>> = stack

    private fun createChild(configuration: Configuration, componentContext: ComponentContext): MainRoot.Child =
        when (configuration) {
            is Configuration.Main -> MainRoot.Child.List(
                listComponent(
                    componentContext,
                    Consumer(::onMainOutput)
                )
            )
            is Configuration.Detail -> MainRoot.Child.Detail(
                detailComponent(
                    componentContext,
                    configuration.pokemon,
                    Consumer(::onDetailOutput)
                )
            )
        }

    private fun onMainOutput(output: ListComponent.Output): Unit =
        when (output) {
            is ListComponent.Output.Selected -> navigation.push(Configuration.Detail(output.pokemon))
        }

    private fun onDetailOutput(output: DetailComponent.Output): Unit =
        when (output) {
            is DetailComponent.Output.List -> navigation.pop()
            else -> {}
        }

    private sealed class Configuration : Parcelable {
        @Parcelize
        object Main : Configuration()

        @Parcelize
        data class Detail(val pokemon: Pokemon) : Configuration()
    }
}