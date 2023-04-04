package com.example.home.list

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.R

@Composable
fun PokemonTopBar(
    onFavoriteFilterClicked: (Boolean) -> Unit,
    onSearchTextInput: (String) -> Unit
) {
    var isSearchingIntended by remember { mutableStateOf(false) }
    var isFavoriteExpanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .padding(all = 12.dp)
                    .clickable { isFavoriteExpanded = isFavoriteExpanded.not() },
                contentDescription = "",
                painter = painterResource(id = R.drawable.baseline_filter_list_24)
            )
            TopBarTextOrSearchArea(
                isSearchingIntended = isSearchingIntended.not(),
                onSearchTextInput = onSearchTextInput,
                onCloseClicked = { isSearchingIntended = false },
                onSearchIconExpand = { isSearchingIntended = true },
            )

        }
        DropdownFilterMenu(
            expanded = isFavoriteExpanded,
            onFavoriteFilterClicked = onFavoriteFilterClicked,
            onDismissRequest = { isFavoriteExpanded = false },
            onSearchTextInput = onSearchTextInput
        )
    }
}

@Composable
fun BoxScope.DropdownFilterMenu(
    expanded: Boolean,
    onFavoriteFilterClicked: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    onSearchTextInput: (String) -> Unit
) {
    var isFavoriteFilter by remember { mutableStateOf(false) }
    DropdownMenu(
        modifier = Modifier.align(Alignment.BottomEnd),
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            text = { Text(text = "Reset Filter") },
            onClick = {
                onFavoriteFilterClicked(false)
                onSearchTextInput("")
            },
            trailingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.reset),
                    contentDescription = ""
                )
            }
        )
        DropdownMenuItem(
            text = { Text(text = "Favorite filter") },
            onClick = {
                isFavoriteFilter = isFavoriteFilter.not()
                onFavoriteFilterClicked(isFavoriteFilter)
            },
            trailingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = if (isFavoriteFilter) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = ""
                )
            }
        )
    }
}

@Composable
fun TopBarTextOrSearchArea(
    isSearchingIntended: Boolean,
    onSearchTextInput: (String) -> Unit,
    onCloseClicked: () -> Unit,
    onSearchIconExpand: () -> Unit
) {
    when(isSearchingIntended) {
        true ->  {
            AnimatedVisibility(
                visible = isSearchingIntended,
                enter = fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Pokemon", fontSize = 22.sp, modifier = Modifier.weight(1f))
                    Icon(
                        modifier = Modifier
                            .padding(all = 12.dp)
                            .clickable { onSearchIconExpand() },
                        contentDescription = "",
                        imageVector = Icons.Filled.Search
                    )
                }
            }
        }
        else -> {
            PokemonSearchBox(
                onSearchTextInput = onSearchTextInput,
                onCloseClicked = onCloseClicked
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonSearchBox(
    onSearchTextInput: (String) -> Unit,
    onCloseClicked: () -> Unit
) {
    var value by remember { mutableStateOf("") }
    TextField(
        value = value,
        onValueChange = {
            value = it
            onSearchTextInput(value)
        },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        leadingIcon = {
            Icon(
                modifier = Modifier,
                contentDescription = "",
                imageVector = Icons.Filled.Search
            )
        },
        trailingIcon = {
            Icon(
                modifier = Modifier
                    .clickable {
                        value = ""
                        onCloseClicked()
                    },
                contentDescription = "",
                imageVector = Icons.Filled.Close
            )
        },
        label = { Text(text = "Search pokemon name")},
        shape = RoundedCornerShape(40.dp),
        // TODO
//        colors = TextFieldDefaults.textFieldColors(
//            textColor = Gray,
//            disabledTextColor = Color.Transparent,
//            backgroundColor = White,
//            focusedIndicatorColor = Color.Transparent,
//            unfocusedIndicatorColor = Color.Transparent,
//            disabledIndicatorColor = Color.Transparent
//        )
    )
}

@Preview
@Composable
private fun PreviewTopAppBar() {
    PokemonTopBar(onFavoriteFilterClicked = {}, onSearchTextInput = {})
}

@Preview
@Composable
private fun PreviewPokemonSearchBox() {
    PokemonSearchBox(onSearchTextInput = {}, onCloseClicked = {})
}