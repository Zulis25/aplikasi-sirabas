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

@Composable
fun PersiapanPengirimanScreen(
    pesanan: ProductionOrderUi,
    kodeKarungTerpindai: List<String>,
    onKembali: () -> Unit,
    onPindaiKarung: () -> Unit
) {
    // Setiap kode mewakili satu karung fisik.
    val daftarKode = kodeKarungTerpindai.distinct()
    val sudahDipindai = daftarKode.size
    val sisa = (pesanan.jumlahKarung - sudahDipindai).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onKembali) {
            Text("‹ Kembali ke Beranda", color = AccessColors.Green)
        }

        Text(
            text = "Persiapan pengiriman",
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
                Text(
                    text = "${pesanan.namaProduk} · ${pesanan.beratPerKarungKg} kg/karung",
                    color = Color.White
                )
                Text(
                    text = "Jumlah pesanan: ${pesanan.jumlahKarung} karung",
                    color = Color.White
                )
            }
        }

        AccessCard {
            Text(
                text = "Pemeriksaan karung",
                color = AccessColors.Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$sudahDipindai dari ${pesanan.jumlahKarung} karung sudah dipindai",
                color = AccessColors.Ink
            )
            Text(
                text = if (sisa > 0) {
                    "Masih perlu dipindai: $sisa karung"
                } else {
                    "Jumlah karung pesanan sudah terpenuhi."
                },
                color = AccessColors.Green,
                fontWeight = FontWeight.SemiBold
            )
        }

        Button(
            onClick = onPindaiKarung,
            enabled = sisa > 0,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccessColors.Green,
                contentColor = Color.White
            )
        ) {
            Text("Pindai label karung")
        }

        Text(
            text = "Karung terakhir dipindai",
            color = AccessColors.Ink,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        AccessCard {
            if (daftarKode.isEmpty()) {
                Text(
                    text = "Belum ada karung yang dipindai untuk pesanan ini.",
                    color = AccessColors.Muted
                )
            } else {
                daftarKode.takeLast(3).asReversed().forEach { kode ->
                    Text(
                        text = kode,
                        color = AccessColors.Ink,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Produksi · Persiapan pengiriman",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PersiapanPengirimanPreview() {
    SIRABASTheme {
        PersiapanPengirimanScreen(
            pesanan = ProductionOrderUi(
                nomorPesanan = "PS-001",
                namaPelanggan = "MBG A",
                namaProduk = "Premium hijau",
                beratPerKarungKg = 25,
                jumlahKarung = 50
            ),
            kodeKarungTerpindai = listOf(
                "PR002-K001",
                "PR002-K002",
                "PR002-K003"
            ),
            onKembali = {},
            onPindaiKarung = {}
        )
    }
}