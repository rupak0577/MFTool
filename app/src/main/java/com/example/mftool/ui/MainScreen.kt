package com.example.mftool.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mftool.MainViewModel
import com.example.mftool.ui.theme.MFToolTheme
import com.example.mftool.vo.IsinObject
import de.charlex.compose.RevealDirection
import de.charlex.compose.RevealSwipe
import de.charlex.compose.rememberRevealState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MainRoot(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiData by viewModel.uiData.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val rootCoroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier.fillMaxSize(),
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (uiData.isEmpty())
                    viewModel.fetchDetails(true)
                else
                    viewModel.fetchDetails(false)
            }) {
                Icon(Icons.Default.Refresh, "refresh")
            }
        }
    ) { innerPadding ->
        MainScreen(innerPadding, uiState, uiData, snackbarHostState, rootCoroutineScope)
    }
}


@Composable
private fun MainScreen(
    padding: PaddingValues,
    uiState: MainViewModel.UiState,
    uiData: List<IsinObject>,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    var dialogState by remember { mutableStateOf<IsinObject?>(null) }

    if (dialogState != null) {
        ShowContentDialog(dialogState) {
            dialogState = null
        }
    }

    when (uiState) {
        is MainViewModel.UiState.Loaded -> {
            Column(
                Modifier.padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Funds: ${uiData.size}")
                }

                if (uiData.isEmpty()) {
                    Text("No data.")
                } else {
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        items(uiData.sortedByDescending { it.nav - it.peak }) {
                            ListItem(it) { isinObject ->
                                dialogState = isinObject
                            }
                        }
                    }
                }
            }
        }

        is MainViewModel.UiState.Loading -> {
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is MainViewModel.UiState.Error -> {
            LaunchedEffect(uiState.error) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(uiState.error ?: "")
                }
            }
        }
    }
}

@Composable
fun ListItem(
    data: IsinObject,
    modifier: Modifier = Modifier,
    showDetails: (data: IsinObject) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    RevealSwipe(
        modifier = Modifier.padding(vertical = 5.dp),
        state = rememberRevealState(
            directions = setOf(
                RevealDirection.EndToStart
            )
        ),
        hiddenContentEnd = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "%.2f".format(data.nav - data.peak),
                    modifier = Modifier
                )
            }
        },
        backgroundCardEndColor = MaterialTheme.colorScheme.secondaryContainer,
        backgroundCardStartColor = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.small,
        card = { shape, content ->
            Card(
                modifier = Modifier.matchParentSize(),
                colors = CardDefaults.cardColors(
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    containerColor = Color.Transparent
                ),
                shape = shape,
                content = content
            )
        },
        backgroundStartActionLabel = null,
        backgroundEndActionLabel = "Hidden",
        onContentLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showDetails(data)
        }
    ) {
        Card(
            colors = CardDefaults.cardColors(
                contentColor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = CardDefaults.elevatedCardElevation(),
            shape = it,
            modifier = Modifier.wrapContentSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp),
            ) {
                Text(text = data.schemeName)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "NAV : ${data.nav}")

                    Text(text = data.date)
                }
            }
        }
    }
}

@Composable
fun ShowContentDialog(isinObject: IsinObject?, onDismissDialog: () -> Unit) {
    Dialog(onDismissRequest = { onDismissDialog() }) {
        Card(
            modifier = Modifier.wrapContentSize(),
            shape = RoundedCornerShape(16.dp),
        ) {
            TableScreen(
                arrayListOf(
                    "Fund House" to (isinObject?.fundHouse ?: ""),
                    "Scheme" to (isinObject?.schemeCategory ?: ""),
                    "Scheme Type" to (isinObject?.schemeType ?: ""),
                    "Peak NAV" to (isinObject?.peak.toString()),
                    "Peak NAV Date" to (isinObject?.peakDate ?: "")
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val items = arrayListOf<IsinObject>()
    items.add(
        IsinObject(
            1,
            "30-06-2025",
            1.0,
            "PPFAS",
            "Parag Parikh ELSS Tax Saver Fund- Direct Growth",
            "Open Ended Schemes",
            "Equity Scheme - ELSS",
            0.0,
            peakDate = "28-09-2024"
        )
    )

    MFToolTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(title = {
                    Text("MFTool")
                })
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                FloatingActionButton(onClick = {

                }) {
                    Icon(Icons.Default.Refresh, "refresh")
                }
            }) { padding ->
            MainScreen(
                padding = padding,
                uiState = MainViewModel.UiState.Loaded,
                uiData = items,
                snackbarHostState = SnackbarHostState(),
                coroutineScope = rememberCoroutineScope()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShowContentDialogPreview() {
    val obj = IsinObject(
        1,
        "30-06-2025",
        1.0,
        "PPFAS",
        "Parag Parikh ELSS Tax Saver Fund- Direct Growth",
        "Open Ended Schemes",
        "Equity Scheme - ELSS",
        0.0,
        peakDate = "28-09-2024"
    )

    ShowContentDialog(obj) { }
}