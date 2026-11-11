package com.stb.sirabas.ui.screens.akun

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun TentangAplikasiScreen(
    onKembali: () -> Unit
) {
    val warna = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(warna.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onKembali) {
            Text("‹ Kembali ke akun")
        }

        Text(
            text = "Tentang aplikasi",
            color = warna.onBackground,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF123F2E)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_stb),
                        contentDescription = "Logo PT. Sri Tani Berkah",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(88.dp)
                            .padding(8.dp)
                    )
                }

                Text(
                    text = "SIRABAS",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Sistem Informasi Produksi, Persediaan, dan Penjualan Beras",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )

                Text(
                    text = "PT. Sri Tani Berkah",
                    color = Color(0xFFE3CB91),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Kegunaan aplikasi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "SIRABAS dirancang untuk mencatat perjalanan beras " +
                            "sejak bahan baku diterima, diolah, dan dikemas hingga " +
                            "dikirim kepada pelanggan. Kode lot, batch produksi, " +
                            "dan label karung membantu petugas menelusuri asal " +
                            "produk pada setiap tahap.",
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Preview(
    name = "Akun · Tentang aplikasi",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun TentangAplikasiPreview() {
    SIRABASTheme {
        TentangAplikasiScreen(onKembali = {})
    }
}