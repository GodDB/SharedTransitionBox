package com.example.lookaheadlayouttest

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentWithReceiverOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
                        val items = remember {
                            colors.map { color ->
                                movableContentWithReceiverOf<SharedElementTransitionBoxScope> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .sharedElement(1000)
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

                        SharedElementTransitionBox(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            var state by remember {
                                mutableStateOf(ViewState.COLUMN)
                            }

                            BackHandler {
                                if (state == ViewState.DETAIL) {
                                    state = ViewState.COLUMN
                                }
                            }

                            Box(modifier = Modifier.fillMaxSize()) {
                                SpeculationWithMovableContentDemo(
                                    items = items,
                                    viewState = state,
                                    onClickState = {
                                        state = it
                                    }
                                )

                                if (state == ViewState.DETAIL) {
                                    Detail(items.get(1))
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun SharedElementTransitionBoxScope.SpeculationWithMovableContentDemo(
        items: List<@Composable SharedElementTransitionBoxScope.() -> Unit>,
        viewState: ViewState,
        onClickState: (ViewState) -> Unit,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.clickable { onClickState.invoke(ViewState.COLUMN) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(viewState == ViewState.COLUMN, { onClickState.invoke(ViewState.COLUMN) })
                    Text("Column")
                }
                Row(
                    modifier = Modifier.clickable { onClickState.invoke(ViewState.ROW) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(viewState == ViewState.ROW, { onClickState.invoke(ViewState.ROW) })
                    Text("2 Row")
                }

                Row(
                    modifier = Modifier.clickable { onClickState.invoke(ViewState.PANNING) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(viewState == ViewState.PANNING, { onClickState.invoke(ViewState.PANNING) })
                    Text("Panning")
                }


                Row(
                    modifier = Modifier.clickable { onClickState.invoke(ViewState.Expand) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(viewState == ViewState.Expand, { onClickState.invoke(ViewState.Expand) })
                    Text("Expand")
                }

                Button(onClick = { onClickState.invoke(ViewState.DETAIL) }) {
                    Text(text = "디테일 화면 보기")
                }
            }
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
                                item()
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
                                        item()
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
                                        item()
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
                                        item()
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
                                        item()
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
                                item()
                            }
                        }
                    }
                }
                ViewState.DETAIL -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            items.forEachIndexed { id, item ->
                                if (id % 2 == 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                    ) {
                                        item()
                                    }
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            items.forEachIndexed { id, item ->
                                if (id % 2 != 0 && id != 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                    ) {
                                        item()
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun SharedElementTransitionBoxScope.Detail(item: @Composable SharedElementTransitionBoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visibleState = remember {
                MutableTransitionState(false).apply {
                    targetState = true
                }
            },
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Spacer(modifier = Modifier.fillMaxSize().background(Color.White))
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(200.dp)) {
                item()
            }

            AnimatedVisibility(
                visibleState = remember {
                    MutableTransitionState(false).apply {
                        targetState = true
                    }
                },
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(text = "디테일 문구들이다아아아아아아아아아이;버라ㅣㅓㅂㄷㅈ하ㅣㅓㅂ디ㅏ헙다ㅣ러ㅏㅣㅂ저라ㅣㅂ저라ㅣㅂ저라ㅣ벚라ㅣ버허ㅣㅂㄷ라ㅣㅓㅂㅇ;ㅏㅓㅈ비아ㅓ바ㅣ어ㅏㅣㅂ저라ㅣㅂㄹ")
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
    COLUMN, ROW, PANNING, Expand, DETAIL
}
