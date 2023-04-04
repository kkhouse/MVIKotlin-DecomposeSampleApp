package com.example.home.list

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.WhitePoint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.example.core.R
import com.example.model.Pokemon
import com.example.model.ProcessResult
import com.example.model.utils.dummyPokemon


@Composable
fun PokemonListScreen(component: ListComponent) {
    val model by component.models.subscribeAsState()
    when (val data = model.items) {
        is ProcessResult.Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
        is ProcessResult.Failure -> {
            Box(Modifier.fillMaxSize()) {
                Text(text = "Error : ${data.error}", modifier = Modifier.align(Alignment.Center))
            }
        }
        is ProcessResult.Success -> {
            PokemonContent(
                pokemons = data.get() ?: emptyList(),
                onPokemonClicked = component::onItemClicked,
                onFavoriteClicked = component::onFavorite,
//                onFavoriteFilterClicked = component::onFavoriteFilter, TODO bugFix
                onFavoriteFilterClicked = {},
                onSearchFilterClicked = component::onSearchPokemon,
//                onPokemonColorUpdate = component::onPokemonColorProvider TODO bugFix
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonContent(
    pokemons: List<Pokemon>,
    onPokemonClicked: (Pokemon) -> Unit,
    onFavoriteClicked: (Pokemon) -> Unit,
    onFavoriteFilterClicked: (Boolean) -> Unit,
    onSearchFilterClicked: (String) -> Unit,
//    onPokemonColorUpdate: (Pokemon) -> Unit
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PokemonTopBar(
                onFavoriteFilterClicked = onFavoriteFilterClicked,
                onSearchTextInput = onSearchFilterClicked
            )
        }
    ) { paddingValues ->
        PokemonListStatelessContent(
            modifier = Modifier.padding(paddingValues),
            pokemons = pokemons,
            onPokemonClicked = onPokemonClicked,
            onFavoriteClicked = onFavoriteClicked,
//            onPokemonColorUpdate = onPokemonColorUpdate
        )
    }
}

@Composable
fun PokemonListStatelessContent(
    modifier: Modifier = Modifier,
    pokemons: List<Pokemon>,
    onPokemonClicked: (Pokemon) -> Unit,
    onFavoriteClicked: (Pokemon) -> Unit,
//    onPokemonColorUpdate: (Pokemon) -> Unit
) {
    var isFavoriteAnim by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        items(pokemons) { pokemon ->
            Column {
                var pokemonColor by remember {
                    mutableStateOf(Color.White)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White,
                                    pokemonColor
                                )
                            )
                        )
                        .clickable { onPokemonClicked(pokemon.copy(pokemonRgbColor = pokemonColor.toArgb())) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncGifImage(
                        modifier = Modifier.padding(all = 12.dp),
                        url = pokemon.sprite,
                        colorProvider =  { color: Color ->
                            pokemonColor = color
                        }
                    )
                    Column(modifier = Modifier
                        .padding(all = 12.dp)
                        .weight(1f)) {
                        Text(text = pokemon.key.rawValue.uppercase())
                        pokemon.types.forEachIndexed { index, value ->
                            Text(text = "type${index+1} : ${value.name}")
                        }
                    }
                    Icon(
                        modifier = Modifier
                            .padding(all = 12.dp)
                            .clickable {
                                // start lottie animation
                                if (pokemon.favorite.not()) isFavoriteAnim = true
                                onFavoriteClicked(pokemon)
                            },
                        contentDescription = "",
                        imageVector = if(pokemon.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder
                    )
                }
                Divider()
            }
        }
    }

    if(isFavoriteAnim) {
        LottieFavoriteOverlay { isFavoriteAnim = false }
    }
}

/*
通信が2回必要だが、現状これしかない様子
 */
@Composable
fun AsyncGifImage(
    modifier: Modifier,
    url: String,
    colorProvider :(Color) -> Unit
) {
    val context = LocalContext.current
    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    var isErrorLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val req = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()
        when(val result = req.context.imageLoader.execute(req)) {
            is SuccessResult -> {
                bitmap = result.drawable.toBitmap()
            } // Bitmap保管
            is ErrorResult -> isErrorLoaded = true
        }
    }

    bitmap?.let {
        LaunchedEffect(Unit) {
            colorProvider(Color(Palette.from(it).generate().lightVibrantSwatch?.rgb ?: 0xFFFFFF))
        }
        AsyncImage(
            modifier = modifier
                .size(56.dp)
                .background(Color.Transparent),
            model = ImageRequest.Builder(context)
                .data(url)
                .placeholder(R.drawable.loading)
                .error(R.drawable.baseline_error_outline_24)
                .crossfade(500)
                .build()
            ,
            contentDescription = "pokemon image",
            imageLoader = ImageLoader.Builder(context)
                .components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                .memoryCache {
                    MemoryCache.Builder(context)
                        .maxSizePercent(0.25)
                        .build()
                }
                .build(),
            contentScale = ContentScale.Fit,
        )
    } ?: run {
        AnimatedVisibility(
            modifier = modifier.size(56.dp),
            visible = bitmap == null,
            enter = fadeIn(animationSpec = tween(durationMillis = 500)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500))
        ) {
            when(isErrorLoaded) {
                true -> Image(painter = painterResource(id = R.drawable.baseline_error_outline_24), contentDescription = "")
                else -> CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun LottieFavoriteOverlay(
    onFinished: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.favorite))
    val progress = animateLottieCompositionAsState(
        composition = composition,
        restartOnPlay = false,
        reverseOnRepeat = true,
        iterations = 2
    )
    LaunchedEffect(progress.isAtEnd) {
        if (progress.isAtEnd) onFinished()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(enabled = false, onClick = {})
    ) {
        LottieAnimation(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(),
            composition = composition,
            progress = { progress.value },
        )
    }
}



@Preview
@Composable
private fun Preview() {
    PokemonListStatelessContent(
        pokemons = listOf(dummyPokemon, dummyPokemon, dummyPokemon),
        onPokemonClicked = {},
        onFavoriteClicked = {},
//        onPokemonColorUpdate = {},
    )
}