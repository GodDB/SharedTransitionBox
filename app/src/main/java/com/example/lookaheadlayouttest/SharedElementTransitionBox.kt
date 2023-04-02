@file:OptIn(ExperimentalComposeUiApi::class)

package com.example.lookaheadlayouttest

import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LookaheadLayout
import androidx.compose.ui.layout.LookaheadLayoutCoordinates
import androidx.compose.ui.layout.LookaheadLayoutScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * 자식 뷰의 변경 (사이즈, 위치)를 캐치해서 자동적으로 shared element transition 애니메이션을 처리해주는 컴포저블
 *
 *  다음과 같이 전환될 때 자동적으로 트랜지션 애니메이션을 수행해준다.
 *
 *
 *  ```
 *  SharedElementTranstionBox {
 *     val content1 = remember {
 *          movableContentWithReceiverOf<SharedElementTransitionBoxScope> {
 *               Spacer(modifier = Modifier.sharedElement())
 *          }
 *      }
 *
 *     val content2 = remember {
 *          movableContentWithReceiverOf<SharedElementTransitionBoxScope> {
 *              Spacer(modifier = Modifier.sharedElement())
 *          }
 *      }
 *
 *    if(showRow) {
 *       Row {
 *          content1()
 *          content2()
 *       }
 *    } else {
 *       Column {
 *          content1()
 *          content2()
 *       }
 *    }
 * }
 * ```
 */

@Composable
fun SharedElementTransitionBox(
    modifier: Modifier,
    content: @Composable SharedElementTransitionBoxScope.() -> Unit
) {
    LookaheadLayout(
        modifier = modifier,
        content = {
            val scope = remember { SharedElementTransitionBoxScope(this) }
            scope.content()
        },
        measurePolicy = { measurable, constraints ->
            val placeables = measurable.map { it.measure(constraints) }
            val width: Int = placeables.maxOf { it.width }
            val height = placeables.maxOf { it.height }
            layout(width, height) {
                placeables.forEach {
                    it.place(0, 0)
                }
            }
        }
    )
}

class SharedElementTransitionBoxScope internal constructor(
    private val scope: LookaheadLayoutScope
) : LookaheadLayoutScope by scope {

    private val offsetAnimatorMap = mutableStateMapOf<Int, Animatable<IntOffset, AnimationVector2D>>()
    private val sizeAnimatorMap = mutableStateMapOf<Int, Animatable<IntSize, AnimationVector2D>>()

    val isRunningTransition = derivedStateOf {
        offsetAnimatorMap.entries.any { it.value.isRunning } || sizeAnimatorMap.entries.any { it.value.isRunning }
    }

    fun Modifier.sharedElement(
        animationDuration: Int = 500,
        onAnimateStart: () -> Unit = {},
        onAnimateFinish: () -> Unit = {},
        onAnimateCancel: () -> Unit = {}
    ): Modifier =
        this.composed {
            val id = remember {
                View.generateViewId()
            }
            DisposableEffect(Unit) {
                onDispose {
                    offsetAnimatorMap.remove(id)
                    sizeAnimatorMap.remove(id)
                }
            }
            val onAnimateStartState = rememberUpdatedState(newValue = onAnimateStart)
            val onAnimateFinishState = rememberUpdatedState(newValue = onAnimateFinish)
            val onAnimateCancelState = rememberUpdatedState(newValue = onAnimateCancel)
            val animationDurationState = rememberUpdatedState(newValue = animationDuration)

            // 자식의 현재 위치
            var curOffset: IntOffset by remember {
                mutableStateOf(IntOffset.Zero)
            }
            // 자식 위치 변경 시 최종적인 자식의 위치값
            var targetOffset: IntOffset? by remember {
                mutableStateOf(null)
            }
            // 자식 사이즈 변경시 최종적인 자식의 사이즈값
            var targetSize: IntSize? by remember {
                mutableStateOf(null)
            }

            // target offset / target size 변경 시 animation 실행한다.
            // 그리고 중간에 target 값이 변경되면 즉시 취소 후 다시 애니메이션 실행한다.
            LaunchedEffect(key1 = Unit) {
                launch {
                    snapshotFlow { targetOffset }
                        .filterNotNull()
                        .collect {
                            launch {
                                val notnullOffsetAnimator = offsetAnimatorMap[id] ?: run {
                                    offsetAnimatorMap[id] = Animatable(
                                        initialValue = it,
                                        typeConverter = IntOffset.VectorConverter
                                    )
                                    return@launch
                                }

                                try {
                                    if (sizeAnimatorMap[id]?.isRunning == false) onAnimateStartState.value.invoke()
                                    notnullOffsetAnimator.animateTo(
                                        targetValue = it,
                                        animationSpec = tween(animationDurationState.value)
                                    )
                                    if (sizeAnimatorMap[id]?.isRunning == false) onAnimateFinishState.value.invoke()
                                } catch (cancellation: CancellationException) {
                                    if (sizeAnimatorMap[id]?.isRunning == false) onAnimateCancelState.value.invoke()
                                    throw cancellation
                                }
                            }
                        }
                }

                launch {
                    snapshotFlow { targetSize }
                        .filterNotNull()
                        .collect {
                            launch {
                                val notnullSizeAnimator = sizeAnimatorMap[id] ?: kotlin.run {
                                    sizeAnimatorMap[id] = Animatable(
                                        initialValue = it,
                                        typeConverter = IntSize.VectorConverter
                                    )
                                    return@launch
                                }

                                try {
                                    if (offsetAnimatorMap[id]?.isRunning == false) onAnimateStartState.value.invoke()
                                    notnullSizeAnimator.animateTo(
                                        targetValue = it,
                                        animationSpec = tween(animationDurationState.value),
                                    )
                                    if (offsetAnimatorMap[id]?.isRunning == false) onAnimateFinishState.value.invoke()
                                } catch (cancellation: CancellationException) {
                                    if (offsetAnimatorMap[id]?.isRunning == false) onAnimateCancelState.value.invoke()
                                    throw cancellation
                                }
                            }
                        }
                }
            }

            /**
             * 호출 순서 : 자식 뷰 변경! -> intermediateLayout() -> onPlaced() -> intermediateLayout-layout()
             */
            this
                .onPlaced { lookaheadScopeCoordinates: LookaheadLayoutCoordinates, layoutCoordinates: LookaheadLayoutCoordinates ->
                    //자식뷰의 최종 위치
                    targetOffset = lookaheadScopeCoordinates
                        .localLookaheadPositionOf(layoutCoordinates)
                        .round()
                    // 현재 자식뷰의 위치
                    curOffset = lookaheadScopeCoordinates
                        .localPositionOf(layoutCoordinates, Offset.Zero)
                        .round()
                }
                .intermediateLayout { measurable, constraints, lookaheadSize ->
                    targetSize = lookaheadSize
                    val (width, height) = sizeAnimatorMap[id]?.value ?: lookaheadSize
                    val animateConstraints = constraints.copy(
                        minWidth = width,
                        maxWidth = width,
                        minHeight = height,
                        maxHeight = height
                    )
                    val placeable = measurable.measure(animateConstraints)
                    layout(placeable.width, placeable.height) {

                        val (offsetX, offsetY) = offsetAnimatorMap[id]?.value?.let {
                            it - curOffset
                        } ?: (targetOffset!! - curOffset)
                        placeable.placeRelative(offsetX, offsetY)
                    }
                }

        }
}
