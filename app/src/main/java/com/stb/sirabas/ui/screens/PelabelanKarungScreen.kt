package com.stb.sirabas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme

@Composable
fun PelabelanKarungScreen(
    kodeBatch: String,
    kodeLot: String,
    tanggalProduksi: String,
    namaProduk: String,
    kemasan: String,
    beratHasil: String,
    jumlahKarung: Int,
    nomorKarung: Int,
    kodeKarung: String,
    onCetakSatu: (String) -> Unit = {},
    onCetakSemua: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pelabelan Karung",
            color = AccessColors.Ink,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF123F2E),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Batch $kodeBatch",
                    color = Color(0xFFE3CB91),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$namaProduk · $kemasan",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Lot bahan $kodeLot", color = Color.White)
                Text("Diproduksi $tanggalProduksi", color = Color.White)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RingkasanKemasan(
                judul = "Berat hasil",
                nilai = beratHasil,
                modifier = Modifier.weight(1f)
            )
            RingkasanKemasan(
                judul = "Jumlah karung",
                nilai = "$jumlahKarung karung",
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Pratinjau label",
            color = AccessColors.Ink,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            contentColor = AccessColors.Ink,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AccessColors.Border)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "PT. Sri Tani Berkah",
                    color = AccessColors.Green,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$namaProduk · $kemasan",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider(color = AccessColors.Border)

                Text("Batch produksi: $kodeBatch")
                Text("Lot bahan asal: $kodeLot")
                Text("Tanggal produksi: $tanggalProduksi")
                Text("Karung $nomorKarung dari $jumlahKarung")

                Text(
                    text = kodeKarung,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Button(
            onClick = { onCetakSatu(kodeKarung) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccessColors.Green,
                contentColor = Color.White
            )
        ) {
            Text("Cetak label karung ini")
        }

        OutlinedButton(
            onClick = { onCetakSemua(kodeBatch) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cetak label satu batch", color = AccessColors.Green)
        }
    }
}

@Composable
private fun RingkasanKemasan(
    judul: String,
    nilai: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = judul,
                color = AccessColors.Muted,
                fontSize = 13.sp
            )
            Text(
                text = nilai,
                color = AccessColors.Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(
    name = "Pelabelan Karung · Petugas Produksi",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PelabelanKarungPreview() {
    SIRABASTheme {
        PelabelanKarungScreen(
            kodeBatch = "PR/001",
            kodeLot = "BM/025",
            tanggalProduksi = "11 November 2026",
            namaProduk = "Premium",
            kemasan = "5 kg",
            beratHasil = "180 kg",
            jumlahKarung = 36,
            nomorKarung = 1,
            kodeKarung = "PR001-K001"
        )
    }
}