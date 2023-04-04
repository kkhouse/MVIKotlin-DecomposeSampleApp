package com.example.reduxarchitectureandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.defaultComponentContext
import com.example.home.root.MainRoot
import com.example.home.root.MainRootComponent
import com.example.home.root.MainRootContent
import com.example.reduxarchitectureandroid.ui.theme.ReduxArchitectureAndroidTheme
import com.example.store.PokemonListStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var store : PokemonListStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReduxArchitectureAndroidTheme {
                MainRootContent(
                    MainRootComponent(
                        componentContext = defaultComponentContext(),
                        store = store
                    )
                )
            }
        }
    }
}