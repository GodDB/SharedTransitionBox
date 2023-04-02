package com.example.lookaheadlayouttest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentWithReceiverOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lookaheadlayouttest.ui.theme.LookaheadLayoutTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LookaheadLayoutTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        SpeculationWithMovableContentDemo()
                    }
                }
            }
        }
    }
}


@Composable
fun SpeculationWithMovableContentDemo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var viewState: ViewState by remember { mutableStateOf(ViewState.COLUMN) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.clickable { viewState = ViewState.COLUMN },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(viewState == ViewState.COLUMN, { viewState = ViewState.COLUMN })
                Text("Column")
            }
            Row(
                modifier = Modifier.clickable { viewState = ViewState.ROW },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(viewState == ViewState.ROW, { viewState = ViewState.ROW })
                Text("2 Row")
            }

            Row(
                modifier = Modifier.clickable { viewState = ViewState.PANNING },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(viewState == ViewState.PANNING, { viewState = ViewState.PANNING })
                Text("Panning")
            }


            Row(
                modifier = Modifier.clickable { viewState = ViewState.Expand },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(viewState == ViewState.Expand, { viewState = ViewState.Expand })
                Text("Expand")
            }
        }

        val items = remember {
            colors.map { color ->
                movableContentWithReceiverOf<SharedElementTransitionBoxScope, ViewState> { viewState ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .sharedElement(1000)
                    ) {
                        AnimatedVisibility(
                            modifier = Modifier.fillMaxSize(),
                            visible = viewState != ViewState.PANNING,
                            enter = fadeIn(tween(1000)),
                            exit = fadeOut(tween(1000))
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, RoundedCornerShape(20))
                            )
                        }
                    }
                }
            }
        }
        SharedElementTransitionBox(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (viewState) {
                ViewState.COLUMN -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                            ) {
                                item(viewState)
                            }
                        }
                    }
                }

                ViewState.ROW -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            items.forEachIndexed { id, item ->
                                if (id % 2 == 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                    ) {
                                        item(viewState)
                                    }
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            items.forEachIndexed { id, item ->
                                if (id % 2 != 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                    ) {
                                        item(viewState)
                                    }
                                }
                            }
                        }
                    }
                }

                ViewState.PANNING -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .offset(x = (-300).dp)
                        ) {
                            items.forEachIndexed { id, item ->
                                if (id % 2 == 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                    ) {
                                        item(viewState)
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .offset(x = (300).dp)
                        ) {
                            items.forEachIndexed { id, item ->
                                if (id % 2 != 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                    ) {
                                        item(viewState)
                                    }
                                }
                            }
                        }
                    }
                }

                ViewState.Expand -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        items.forEachIndexed { id, item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(bottom = 30.dp)
                            ) {
                                item(viewState)
                            }
                        }
                    }
                }
            }

        }
    }
}

private val colors = listOf(
    Color(0xffff6f69),
    Color(0xffffcc5c),
    Color(0xff264653),
    Color(0xff2a9d84)
)

enum class ViewState {
    COLUMN, ROW, PANNING, Expand
}
