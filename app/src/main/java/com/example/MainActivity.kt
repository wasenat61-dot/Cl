package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    CalculatorScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = viewModel()
) {
    val expression by viewModel.expression.collectAsStateWithLifecycle()
    val liveResult by viewModel.liveResult.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    var isHistoryOpen by remember { mutableStateOf(false) }
    var isAboutOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calculator",
                    color = TextSlateLight,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { isAboutOpen = true },
                        modifier = Modifier.testTag("about_developer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Developer",
                            tint = if (isAboutOpen) TextAccent else TextSlateMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { isHistoryOpen = !isHistoryOpen },
                        modifier = Modifier.testTag("toggle_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Show history",
                            tint = if (isHistoryOpen) TextAccent else TextSlateMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Screen Display Area (Expression + Result Preview)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                // Expression Text Field
                Text(
                    text = expression.ifEmpty { "0" },
                    color = TextSlateLight,
                    fontSize = if (expression.length > 12) 32.sp else 44.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 52.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expression_display")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Live Result Preview Text Field
                AnimatedVisibility(
                    visible = liveResult.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = liveResult,
                        color = TextAccent,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("live_result_display")
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Button Keyboard Pad Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Keyboard Rows definition
                val padRows = listOf(
                    listOf(
                        PadButton("C", ButtonType.Action, "button_clear"),
                        PadButton("()", ButtonType.Action, "button_parentheses"),
                        PadButton("%", ButtonType.Action, "button_modulo"),
                        PadButton("÷", ButtonType.Operator, "button_divide")
                    ),
                    listOf(
                        PadButton("7", ButtonType.Number, "button_7"),
                        PadButton("8", ButtonType.Number, "button_8"),
                        PadButton("9", ButtonType.Number, "button_9"),
                        PadButton("×", ButtonType.Operator, "button_multiply")
                    ),
                    listOf(
                        PadButton("4", ButtonType.Number, "button_4"),
                        PadButton("5", ButtonType.Number, "button_5"),
                        PadButton("6", ButtonType.Number, "button_6"),
                        PadButton("−", ButtonType.Operator, "button_subtract")
                    ),
                    listOf(
                        PadButton("1", ButtonType.Number, "button_1"),
                        PadButton("2", ButtonType.Number, "button_2"),
                        PadButton("3", ButtonType.Number, "button_3"),
                        PadButton("+", ButtonType.Operator, "button_add")
                    ),
                    listOf(
                        PadButton("0", ButtonType.Number, "button_0"),
                        PadButton(".", ButtonType.Number, "button_decimal"),
                        PadButton("⌫", ButtonType.Action, "button_backspace"),
                        PadButton("=", ButtonType.Equals, "button_equals")
                    )
                )

                for (row in padRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (btn in row) {
                            CalculatorButton(
                                text = btn.text,
                                type = btn.type,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f)
                                    .testTag(btn.tag),
                                onClick = {
                                    if (btn.text == "=") {
                                        viewModel.onEvaluate()
                                    } else {
                                        viewModel.onInput(btn.text)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Animated drop-down Drawer containing Calculation History
        AnimatedVisibility(
            visible = isHistoryOpen,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isHistoryOpen = false }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .widthIn(max = 600.dp)
                        .align(Alignment.TopCenter)
                        .clickable(enabled = false) {}, // Prevent closing when clicking card inside
                    colors = CardDefaults.cardColors(containerColor = SurfaceObsidian),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Calculation History",
                                color = TextSlateLight,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (history.isNotEmpty()) {
                                TextButton(
                                    onClick = { viewModel.clearHistory() },
                                    colors = ButtonDefaults.textButtonColors(contentColor = ColorAccentPrimary),
                                    modifier = Modifier.testTag("clear_history_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Clear History",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (history.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Your calculation logs are empty",
                                    color = TextSlateMuted,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                                    .testTag("history_list"),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(history, key = { it.id }) { item ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceButtonNormal.copy(alpha = 0.5f))
                                            .clickable {
                                                viewModel.loadFromHistory(item.expression)
                                                isHistoryOpen = false
                                            }
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = item.expression,
                                            color = TextSlateMuted,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "= ${item.result}",
                                            color = TextSlateLight,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // About Developer Dialog Modal showcasing Takirul Islam
        if (isAboutOpen) {
            AlertDialog(
                onDismissRequest = { isAboutOpen = false },
                confirmButton = {
                    TextButton(
                        onClick = { isAboutOpen = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = ColorAccentPrimary)
                    ) {
                        Text("Close", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                },
                title = {
                    Text(
                        text = "About Developer",
                        color = TextSlateLight,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile image with elegant border and circular crop
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(ColorAccentPrimary.copy(alpha = 0.2f))
                                .padding(4.dp)
                                .clip(CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.takirul_islam),
                                contentDescription = "Takirul Islam",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Takirul Islam",
                            color = TextSlateLight,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Lead Designer & Developer",
                            color = TextAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Designed and developed with high-quality Material 3 layout, clean reactive state math parsing, and a stunning dark mode experience.",
                            color = TextSlateMuted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                },
                containerColor = SurfaceObsidian,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

enum class ButtonType {
    Number,
    Operator,
    Action,
    Equals
}

data class PadButton(
    val text: String,
    val type: ButtonType,
    val tag: String
)

@Composable
fun CalculatorButton(
    text: String,
    type: ButtonType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth hover / tap click shrink animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "button_scale"
    )

    val containerColor = when (type) {
        ButtonType.Number -> SurfaceButtonNormal
        ButtonType.Action -> SurfaceButtonFunction
        ButtonType.Operator -> ColorAccentPrimary
        ButtonType.Equals -> ColorAccentSuccess
    }

    val contentColor = when (type) {
        ButtonType.Operator -> TextSlateLight
        ButtonType.Equals -> TextSlateLight
        ButtonType.Action -> TextAccent
        ButtonType.Number -> TextSlateLight
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = if (type == ButtonType.Action && text.length > 1) 20.sp else 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center
        )
    }
}
