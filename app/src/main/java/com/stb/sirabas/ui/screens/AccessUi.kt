package com.stb.sirabas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.R

// Warna dibaca dari tema yang sedang aktif.
internal object AccessColors {

    val Background: Color
        @Composable
        get() = MaterialTheme.colorScheme.background

    val Green: Color
        @Composable
        get() = MaterialTheme.colorScheme.primary

    val Ink: Color
        @Composable
        get() = MaterialTheme.colorScheme.onSurface

    val Muted: Color
        @Composable
        get() = MaterialTheme.colorScheme.onSurfaceVariant

    val Border: Color
        @Composable
        get() = MaterialTheme.colorScheme.outlineVariant

    val SoftGreen: Color
        @Composable
        get() = MaterialTheme.colorScheme.secondaryContainer

    val SoftGold: Color
        @Composable
        get() = MaterialTheme.colorScheme.tertiaryContainer

    val GoldInk: Color
        @Composable
        get() = MaterialTheme.colorScheme.onTertiaryContainer
}

@Composable
internal fun AccessPage(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AccessColors.Background,
        contentColor = AccessColors.Ink
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(
                                R.drawable.sirabas_arrow_left
                            ),
                            contentDescription = "Kembali",
                            tint = AccessColors.Green
                        )
                    }

                    Text(
                        text = title,
                        fontSize = 25.sp,
                        color = AccessColors.Ink,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(
                    color = AccessColors.Border
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_stb),
                        contentDescription = "Logo PT Sri Tani Berkah",
                        modifier = Modifier.size(40.dp)
                    )

                    Text(
                        text = "SIRABAS",
                        fontSize = 22.sp,
                        color = AccessColors.Ink,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                content()
            }
        }
    }
}

@Composable
internal fun AccessNotice(
    title: String,
    message: String,
    gold: Boolean = false
) {
    val latar = if (gold) {
        AccessColors.SoftGold
    } else {
        AccessColors.SoftGreen
    }

    val warnaTeks = if (gold) {
        AccessColors.GoldInk
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = latar,
        contentColor = warnaTeks
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = message,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
internal fun AccessCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.dp,
            color = AccessColors.Border
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}