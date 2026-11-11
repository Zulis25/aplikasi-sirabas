package com.stb.sirabas.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme

@Composable
fun MenuProduksiScreen(
    onBahanBaku: () -> Unit,
    onHasilProduksi: () -> Unit,
    onProdukRetur: () -> Unit,
    onPelabelan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Produksi",
            color = AccessColors.Ink,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        KartuMenuProduksi(
            judul = "Bahan baku masuk",
            deskripsi = "Penerimaan beras dan gabah, supplier, serta hasil pengujian.",
            onClick = onBahanBaku
        )

        KartuMenuProduksi(
            judul = "Hasil produksi",
            deskripsi = "Hasil per batch, kemasan, rendemen, dan susut.",
            onClick = onHasilProduksi
        )

        KartuMenuProduksi(
            judul = "Produk retur",
            deskripsi = "Barang yang dikembalikan dan sisa untuk produksi ulang.",
            onClick = onProdukRetur
        )

        KartuMenuProduksi(
            judul = "Label & Cetak",
            deskripsi = "Label lot bahan baku dan label karung hasil produksi.",
            onClick = onPelabelan
        )
    }
}

@Composable
private fun KartuMenuProduksi(
    judul: String,
    deskripsi: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = AccessColors.Border
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = judul,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccessColors.Ink
                )
                Text(
                    text = deskripsi,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = AccessColors.Muted
                )
            }

            Text(
                text = "›",
                fontSize = 26.sp,
                color = AccessColors.Green
            )
        }
    }
}

@Preview(
    name = "Petugas Produksi · Menu Produksi",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun MenuProduksiPreview() {
    SIRABASTheme {
        MenuProduksiScreen(
            onBahanBaku = {},
            onHasilProduksi = {},
            onProdukRetur = {},
            onPelabelan = {}
        )
    }
}