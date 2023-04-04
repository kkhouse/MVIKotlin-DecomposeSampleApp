package com.example.home.root

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.example.home.detail.PokemonDetail
import com.example.home.list.PokemonListScreen

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun MainRootContent(component: MainRoot) {
    Children(
        stack = component.childStack,
        animation = stackAnimation(fade() + scale())
    ) {
        when(val child = it.instance) {
            is MainRoot.Child.List -> PokemonListScreen(child.component)
            is MainRoot.Child.Detail -> PokemonDetail(child.component)
        }
    }
}