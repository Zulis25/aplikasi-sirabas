package com.stb.sirabas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

data class ProdukReturUi(
    val kode: String,
    val tanggal: String,
    val pelanggan: String,
    val namaProduk: String,
    val kemasan: String,
    val jumlahKarung: Int,
    val beratDiterimaKg: BigDecimal,
    val beratDiprosesKg: BigDecimal,
    val alasan: String
) {
    val sisaKg: BigDecimal
        get() = beratDiterimaKg - beratDiprosesKg
}

@Composable
fun ProdukReturScreen(
    daftarRetur: List<ProdukReturUi>,
    onTambahRetur: () -> Unit
) {
    val totalTersedia = daftarRetur.fold(BigDecimal.ZERO) { total, retur ->
        total + retur.sisaKg
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Produk Retur",
                color = AccessColors.Ink,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
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
                        text = "Tersedia untuk produksi ulang",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${formatKgRetur(totalTersedia)} kg",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onTambahRetur,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Catat retur", color = AccessColors.Green)
            }
        }

        item {
            Text(
                text = "Riwayat retur",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccessColors.Ink
            )
        }

        if (daftarRetur.isEmpty()) {
            item {
                AccessCard {
                    Text(
                        text = "Belum ada produk retur yang tercatat.",
                        color = AccessColors.Muted
                    )
                }
            }
        } else {
            items(
                items = daftarRetur,
                key = { retur -> retur.kode }
            ) { retur ->
                AccessCard {
                    Text(
                        text = "${retur.kode} · ${retur.tanggal}",
                        color = AccessColors.Muted,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${retur.namaProduk} · ${retur.kemasan}",
                        color = AccessColors.Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text("Pelanggan: ${retur.pelanggan}")
                    Text("Alasan retur: ${retur.alasan}")
                    Text(
                        "Diterima: ${retur.jumlahKarung} karung " +
                                "(${formatKgRetur(retur.beratDiterimaKg)} kg)"
                    )

                    if (retur.beratDiprosesKg.signum() > 0) {
                        Text(
                            "Sudah diproses ulang: " +
                                    "${formatKgRetur(retur.beratDiprosesKg)} kg"
                        )
                    }

                    Text(
                        text = "Sisa retur: ${formatKgRetur(retur.sisaKg)} kg",
                        color = AccessColors.Green,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun formatKgRetur(berat: BigDecimal): String =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(berat)

@Preview(
    name = "Produksi · Daftar Produk Retur",
    showBackground = true,
    widthDp = 390,
    heightDp = 900
)
@Composable
private fun ProdukReturPreview() {
    SIRABASTheme {
        ProdukReturScreen(
            daftarRetur = listOf(
                ProdukReturUi(
                    kode = "RT/002",
                    tanggal = "11 November 2026",
                    pelanggan = "MBG A",
                    namaProduk = "Premium",
                    kemasan = "25 kg hijau",
                    jumlahKarung = 2,
                    beratDiterimaKg = BigDecimal("50"),
                    beratDiprosesKg = BigDecimal.ZERO,
                    alasan = "Beras berkutu"
                ),
                ProdukReturUi(
                    kode = "RT/001",
                    tanggal = "10 November 2026",
                    pelanggan = "Toko Beras Jaya",
                    namaProduk = "Medium",
                    kemasan = "25 kg",
                    jumlahKarung = 3,
                    beratDiterimaKg = BigDecimal("75"),
                    beratDiprosesKg = BigDecimal("25"),
                    alasan = "Kemasan rusak"
                )
            ),
            onTambahRetur = {}
        )
    }
}