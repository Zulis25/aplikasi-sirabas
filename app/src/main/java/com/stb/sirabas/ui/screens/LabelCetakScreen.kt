package com.stb.sirabas.ui.screens

import androidx.compose.foundation.BorderStroke
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
fun LabelCetakScreen(
    onKembali: () -> Unit,
    onLabelBahanMasuk: () -> Unit,
    onLabelHasilKemasan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onKembali) {
            Text("‹ Kembali ke Produksi", color = AccessColors.Green)
        }

        Text(
            text = "Label & Cetak",
            color = AccessColors.Ink,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        KartuPilihanLabel(
            judul = "Label bahan masuk",
            deskripsi = "Pilih lot penerimaan untuk melihat dan mencetak label beras atau gabah.",
            onClick = onLabelBahanMasuk
        )

        KartuPilihanLabel(
            judul = "Label hasil kemasan",
            deskripsi = "Pilih batch produksi untuk melihat dan mencetak label setiap karung.",
            onClick = onLabelHasilKemasan
        )
    }
}

@Composable
private fun KartuPilihanLabel(
    judul: String,
    deskripsi: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, AccessColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = judul,
                    color = AccessColors.Ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = deskripsi,
                    color = AccessColors.Muted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Text(
                text = "›",
                color = AccessColors.Green,
                fontSize = 26.sp
            )
        }
    }
}

@Preview(
    name = "Produksi · Label & Cetak",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun LabelCetakPreview() {
    SIRABASTheme {
        LabelCetakScreen(
            onKembali = {},
            onLabelBahanMasuk = {},
            onLabelHasilKemasan = {}
        )
    }
}