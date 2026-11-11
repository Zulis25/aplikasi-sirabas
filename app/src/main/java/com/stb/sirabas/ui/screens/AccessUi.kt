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

internal object AccessColors {
    val Background = Color(0xFFF7F7F0)
    val Green = Color(0xFF236547)
    val Ink = Color(0xFF183B30)
    val Muted = Color(0xFF627369)
    val Border = Color(0xFFE1E6DB)
    val SoftGreen = Color(0xFFEAF4EE)
    val SoftGold = Color(0xFFFFF2D9)
    val GoldInk = Color(0xFF805B15)
}

@Composable
internal fun AccessPage(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
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
                        contentDescription = "Kembali ke login",
                        tint = AccessColors.Green
                    )
                }

                Text(
                    text = title,
                    fontSize = 25.sp,
                    color = AccessColors.Ink
                )
            }

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

@Composable
internal fun AccessNotice(
    title: String,
    message: String,
    gold: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (gold) {
            AccessColors.SoftGold
        } else {
            AccessColors.SoftGreen
        }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val ink = if (gold) {
                AccessColors.GoldInk
            } else {
                AccessColors.Ink
            }

            Text(
                text = title,
                color = ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = message,
                color = ink,
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
        color = Color.White,
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