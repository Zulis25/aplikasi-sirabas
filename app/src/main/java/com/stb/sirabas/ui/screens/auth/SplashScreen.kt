package com.stb.sirabas.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.R
import com.stb.sirabas.ui.theme.SIRABASTheme

@Composable
fun SplashScreen() {
    val warnaEmas = Color(0xFFE3CB91)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF245B49),
                        Color(0xFF173F35),
                        Color(0xFF0C2923)
                    )
                )
            )
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 28.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(184.dp),
                shape = CircleShape,
                color = warnaEmas.copy(alpha = 0.08f),
                border = BorderStroke(
                    width = 1.dp,
                    color = warnaEmas.copy(alpha = 0.45f)
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(158.dp),
                        shape = CircleShape,
                        color = Color(0xFFFFFDF6)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    id = R.drawable.logo_stb
                                ),
                                contentDescription =
                                    "Logo PT. Sri Tani Berkah",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(134.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Text(
                text = "SIRABAS",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = "Sistem Informasi Produksi,\n" +
                        "Persediaan dan Penjualan Beras",
                color = Color(0xFFD2E3D9),
                fontSize = 14.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Box(
                modifier = Modifier
                    .size(width = 46.dp, height = 2.dp)
                    .background(warnaEmas)
            )
        }

        Text(
            text = "PT. SRI TANI BERKAH",
            color = warnaEmas,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    horizontal = 24.dp,
                    vertical = 32.dp
                )
        )
    }
}

// PREVIEW SPLASH
@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun SplashScreenPreview() {
    SIRABASTheme {
        SplashScreen()
    }
}