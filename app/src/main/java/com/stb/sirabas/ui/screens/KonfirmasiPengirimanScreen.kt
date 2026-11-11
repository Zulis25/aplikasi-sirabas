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
fun KonfirmasiPengirimanScreen(
    pesanan: ProductionOrderUi,
    kodeKarungSesuai: List<String>,
    onKembali: () -> Unit,
    onKonfirmasi: () -> Unit
) {
    // Daftar ini berisi kode karung yang sudah lolos pemeriksaan pesanan.
    val jumlahSesuai = kodeKarungSesuai.distinct().size
    val jumlahPesanan = pesanan.jumlahKarung
    val lengkap = jumlahPesanan > 0 && jumlahSesuai == jumlahPesanan

    val totalBeratKg = jumlahPesanan.toLong() * pesanan.beratPerKarungKg
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
        TextButton(onClick = onKembali) {
            Text("‹ Kembali ke persiapan pengiriman", color = AccessColors.Green)
        }

        Text(
            text = "Konfirmasi pengiriman",
            color = AccessColors.Ink,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF123F2E)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Pesanan ${pesanan.nomorPesanan}",
                    color = Color(0xFFE3CB91),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = pesanan.namaPelanggan,
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AccessCard {
            Text(
                text = "Barang yang akan dikirim",
                color = AccessColors.Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${pesanan.namaProduk} · ${pesanan.beratPerKarungKg} kg/karung",
                color = AccessColors.Ink
            )
            Text("Jumlah pesanan: $jumlahPesanan karung")
            Text("Total berat pesanan: ${formatAngka.format(totalBeratKg)} kg")
        }

        AccessCard {
            Text(
                text = "Hasil pemeriksaan label",
                color = AccessColors.Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$jumlahSesuai dari $jumlahPesanan karung sesuai",
                color = AccessColors.Ink,
                fontWeight = FontWeight.SemiBold
            )

            val keterangan = when {
                lengkap -> "Semua karung dalam pesanan sudah diperiksa."
                jumlahSesuai < jumlahPesanan ->
                    "Masih ada ${jumlahPesanan - jumlahSesuai} karung yang perlu dipindai."
                else -> "Periksa kembali jumlah karung dan rincian pesanan."
            }

            Text(
                text = keterangan,
                color = if (lengkap) AccessColors.Green else AccessColors.Muted
            )
        }

        Button(
            onClick = onKonfirmasi,
            enabled = lengkap,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccessColors.Green,
                contentColor = Color.White
            )
        ) {
            Text("Konfirmasi barang keluar")
        }
    }
}

@Preview(
    name = "Produksi · Konfirmasi pengiriman",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun KonfirmasiPengirimanPreview() {
    val pesanan = ProductionOrderUi(
        nomorPesanan = "PS-001",
        namaPelanggan = "MBG A",
        namaProduk = "Premium hijau",
        beratPerKarungKg = 25,
        jumlahKarung = 50
    )

    val contohKodeKarung = (1..50).map { nomor ->
        "PR002-K${nomor.toString().padStart(3, '0')}"
    }

    SIRABASTheme {
        KonfirmasiPengirimanScreen(
            pesanan = pesanan,
            kodeKarungSesuai = contohKodeKarung,
            onKembali = {},
            onKonfirmasi = {}
        )
    }
}