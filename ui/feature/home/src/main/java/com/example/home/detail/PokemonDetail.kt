package com.example.home.detail

import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.lerp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.core.R
import com.example.model.Pokemon
import com.example.model.utils.dummyPokemon
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val TitleHeight = 128.dp
private val GradientScroll = 180.dp
private val ImageOverlap = 115.dp
private val MinTitleOffset = 56.dp
private val MinImageOffset = 12.dp
private val MaxTitleOffset = ImageOverlap + MinTitleOffset + GradientScroll
private val ExpandedImageSize = 300.dp
private val CollapsedImageSize = 150.dp
private val HzPadding = Modifier.padding(horizontal = 24.dp)

/*
refer to the code in "com.example.jetsnack.ui.snackdetail"
 */
@Composable
fun PokemonDetail(
    component: DetailComponent
) {
    val pokemon = component.models.value.pokemon
    Box(Modifier.fillMaxSize()) {
        val scroll = rememberScrollState(1000) // TODO 表示部品が多くなったら0に戻す
        Header(pokemonColor = Color(pokemon.pokemonRgbColor))
        Body(pokemon, scroll)
        Title(pokemon) { scroll.value }
        Image(pokemon.sprite) { scroll.value }
        Up(component::onCloseClicked)
    }
}

@Composable
private fun Header(pokemonColor: Color) {
    Spacer(
        modifier = Modifier
            .height(280.dp)
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Color.White, pokemonColor)))
    )
}

@Composable
private fun Up(upPress: () -> Unit) {
    IconButton(
        onClick = upPress,
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .size(36.dp)
            .background(
                color = Color.Gray.copy(alpha = 0.32f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = mirroringBackIcon(),
            contentDescription = ""
        )
    }
}

@Composable
private fun Title(pokemon: Pokemon, scrollProvider: () -> Int) {
    val maxOffset = with(LocalDensity.current) { MaxTitleOffset.toPx() }
    val minOffset = with(LocalDensity.current) { MinTitleOffset.toPx() }

    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .heightIn(min = TitleHeight)
            .statusBarsPadding()
            .offset {
                val scroll = scrollProvider()
                val offset = (maxOffset - scroll).coerceAtLeast(minOffset)
                IntOffset(x = 0, y = offset.toInt())
            }
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = pokemon.species,
            style = MaterialTheme.typography.h4,
            modifier = HzPadding
        )
        pokemon.types.forEach {
            Text(
                text =  it.name,
                style = MaterialTheme.typography.subtitle2,
                fontSize = 20.sp,
                modifier = HzPadding
            )
        }
        Spacer(Modifier.height(4.dp))
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Image(
    imageUrl: String,
    scrollProvider: () -> Int
) {
    val collapseRange = with(LocalDensity.current) { (MaxTitleOffset - MinTitleOffset).toPx() }
    val collapseFractionProvider = {
        (scrollProvider() / collapseRange).coerceIn(0f, 1f)
    }
    val context = LocalContext.current

    CollapsingImageLayout(
        collapseFractionProvider = collapseFractionProvider,
        modifier = HzPadding.then(Modifier.statusBarsPadding())
    ) {
        AsyncImage(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp)),
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .placeholder(R.drawable.loading)
                .error(R.drawable.baseline_error_outline_24)
                .crossfade(500)
                .build()
            ,
            contentDescription = "pokemon image",
            imageLoader = ImageLoader.Builder(context)
                .components {
                    if (SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                .build(),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun Body(
    pokemon: Pokemon,
    scroll: ScrollState
) {
    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(MinTitleOffset)
        )
        Column(
            modifier = Modifier.verticalScroll(scroll)
        ) {
            Spacer(Modifier.height(GradientScroll))
            Column {
                Spacer(Modifier.height(ImageOverlap))
                Spacer(Modifier.height(TitleHeight))

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Base Status",
                    fontSize = 28.sp,
                    modifier = HzPadding
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Total : ${pokemon.baseStatsTotal}",
                    style = MaterialTheme.typography.subtitle1,
                    fontSize = 24.sp,
                    modifier = HzPadding,
                )

                Spacer(modifier = Modifier.height(16.dp))
                PokemonStatus(
                    modifier = HzPadding,
                    prefixText = "HP",
                    status = pokemon.baseStats.hp.toFloat(),
                    barColor = Color(0xFFFF0000).copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                PokemonStatus(
                    modifier = HzPadding,
                    prefixText = "Attack",
                    status = pokemon.baseStats.attack.toFloat(),
                    barColor = Color(0xFFFFA500).copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                PokemonStatus(
                    modifier = HzPadding,
                    prefixText = "Defense",
                    status = pokemon.baseStats.defense.toFloat(),
                    barColor = Color(0xFF008000).copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                PokemonStatus(
                    modifier = HzPadding,
                    prefixText = "SpecialAttack",
                    status = pokemon.baseStats.specialattack.toFloat(),
                    barColor = Color(0xFF0000FF).copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                PokemonStatus(
                    modifier = HzPadding,
                    prefixText = "SpecialDefense",
                    status = pokemon.baseStats.specialdefense.toFloat(),
                    barColor = Color(0xFF800080).copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                PokemonStatus(
                    modifier = HzPadding,
                    prefixText = "Speed",
                    status = pokemon.baseStats.speed.toFloat(),
                    barColor = Color(0xFFFFFF00).copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(ImageOverlap))
            }
        }
    }
}

@Composable
fun PokemonStatus(
    modifier: Modifier,
    prefixText: String = "Attack",
    status: Float,
    maxStatus: Float = 130f,
    barColor: Color
) {
    var statusBarWidth by remember { mutableStateOf(0f) }
    val animateStatusBarWidth by animateFloatAsState(
        targetValue = statusBarWidth,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 500
        ),
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                statusBarWidth = this.size.width * (status / maxStatus)
                drawRoundRect( // ストローク
                    color = Color.Gray.copy(alpha = 0.4f),
                    cornerRadius = CornerRadius(x = 80f, y = 80f),
                    topLeft = Offset(x = 0f, y = 20f),
                    size = Size(height = this.size.height - 40f, width = this.size.width)
                )
                drawRoundRect( //
                    color = barColor,
                    cornerRadius = CornerRadius(x = 80f, y = 80f),
                    topLeft = Offset(x = 0f, y = 20f),
                    size = Size(height = this.size.height - 40f, width = animateStatusBarWidth)
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "$prefixText  ${status.toInt()} / ${maxStatus.toInt()}",
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
    
}

@Composable
private fun CollapsingImageLayout(
    collapseFractionProvider: () -> Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        check(measurables.size == 1)

        val collapseFraction = collapseFractionProvider()

        val imageMaxSize = min(ExpandedImageSize.roundToPx(), constraints.maxWidth)
        val imageMinSize = max(CollapsedImageSize.roundToPx(), constraints.minWidth)
        val imageWidth = lerp(imageMaxSize, imageMinSize, collapseFraction)
        val imagePlaceable = measurables[0].measure(Constraints.fixed(imageWidth, imageWidth))

        val imageY = lerp(MinTitleOffset, MinImageOffset, collapseFraction).roundToPx()
        val imageX = lerp(
            (constraints.maxWidth - imageWidth) / 2, // centered when expanded
            constraints.maxWidth - imageWidth, // right aligned when collapsed
            collapseFraction
        )
        layout(
            width = constraints.maxWidth,
            height = imageY + imageWidth
        ) {
            imagePlaceable.placeRelative(imageX, imageY)
        }
    }
}

@Composable
fun mirroringIcon(ltrIcon: ImageVector, rtlIcon: ImageVector): ImageVector =
    if (LocalLayoutDirection.current == LayoutDirection.Ltr) ltrIcon else rtlIcon
@Composable
fun mirroringBackIcon() = mirroringIcon(
    ltrIcon = Icons.Outlined.ArrowBack, rtlIcon = Icons.Outlined.ArrowForward
)

@Preview
@Composable
private fun Preview() {
    val pokemon = dummyPokemon
    Box(Modifier.fillMaxSize()) {
        val scroll = rememberScrollState(0)
        Header(Color.Cyan)
        Body(pokemon, scroll)
        Title(pokemon) { scroll.value }
        Image(pokemon.sprite) { scroll.value }
        Up {}
    }
}


@Preview
@Composable
private fun PokemonStatusPreview() {
    PokemonStatus(
        modifier = Modifier,
        status = 80F,
        barColor = Color.Red.copy(alpha = 0.6f)
    )
}