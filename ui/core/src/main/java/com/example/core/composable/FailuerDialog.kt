package com.example.core.composable

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.model.AppError

@Composable
fun FailureDialog(error: AppError) {
    AlertDialog(
        modifier = Modifier,
        title = { Text("Error") },
        text = { Text(error.toString()) },
        buttons = {
            Button(onClick = {}) {
                Text("OK")
            }
        },
        onDismissRequest = {}
    )
}