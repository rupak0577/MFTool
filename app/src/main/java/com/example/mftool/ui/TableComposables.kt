package com.example.mftool.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float
) {
    Text(
        text = text,
        minLines = 2,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .border(1.dp, Color.Black)
            .weight(weight)
            .padding(8.dp)
            .fillMaxSize()
    )
}

// https://stackoverflow.com/a/68143597
@Composable
fun TableScreen(map: List<Pair<String, String>>) {
    // Each cell of a column must have the same weight.
    val column1Weight = .3f // 30%
    val column2Weight = .7f // 70%

    // The LazyColumn will be our table. Notice the use of the weights below
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        item {
            Row(Modifier.background(Color.LightGray)) {
                TableCell(text = "", weight = column1Weight)
                TableCell(text = "Values", weight = column2Weight)
            }
        }
        // Rows
        items(map) {
            val (key, value) = it

            Row(Modifier.fillMaxWidth()) {
                TableCell(text = key, weight = column1Weight)
                TableCell(text = value, weight = column2Weight)
            }
        }
    }
}