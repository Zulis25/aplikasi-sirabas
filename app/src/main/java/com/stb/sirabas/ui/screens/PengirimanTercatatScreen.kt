package com.stb.sirabas.ui.screens

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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PengirimanTercatatScreen(
    pesanan: ProductionOrderUi,
    jumlahKarungKeluar: Int,
    waktuKeluar: String,
    onKembaliKeBeranda: () -> Unit
) {
    val totalBeratKg = jumlahKarungKeluar.toLong() * pesanan.beratPerKarungKg
    val formatAngka = NumberFormat.getIntegerInstance(
        Locale.forLanguageTag("id-ID")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pengiriman tercatat",
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
                    text = "Barang keluar dari gudang",
                    color = Color(0xFFE3CB91),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = pesanan.namaPelanggan,
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pesanan ${pesanan.nomorPesanan}",
                    color = Color.White
                )
                Text(text = waktuKeluar, color = Color.White)
            }
        }

        AccessCard {
            Text(
                text = "Rincian barang keluar",
                color = AccessColors.Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${pesanan.namaProduk} · ${pesanan.beratPerKarungKg} kg/karung",
                color = AccessColors.Ink
            )

            HorizontalDivider(color = AccessColors.Border)

            Text("Jumlah keluar: $jumlahKarungKeluar karung")
            Text(
                text = "Total berat: ${formatAngka.format(totalBeratKg)} kg",
                color = AccessColors.Ink,
                fontWeight = FontWeight.SemiBold
            )
        }

        Button(
            onClick = onKembaliKeBeranda,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccessColors.Green,
                contentColor = Color.White
            )
        ) {
            Text("Kembali ke beranda produksi")
        }
    }
}

@Preview(
    name = "Produksi · Barang keluar tercatat",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PengirimanTercatatPreview() {
    SIRABASTheme {
        PengirimanTercatatScreen(
            pesanan = ProductionOrderUi(
                nomorPesanan = "PS-001",
                namaPelanggan = "MBG A",
                namaProduk = "Premium hijau",
                beratPerKarungKg = 25,
                jumlahKarung = 50
            ),
            jumlahKarungKeluar = 50,
            waktuKeluar = "11 November 2026 · 09.15 WITA",
            onKembaliKeBeranda = {}
        )
    }
}