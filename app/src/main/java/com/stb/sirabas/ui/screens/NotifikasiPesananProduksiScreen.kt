package com.stb.sirabas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class ItemPersiapanProduksiUi(
    val namaProduk: String,
    val kemasan: String,
    val jumlahPesanan: Int,
    val jumlahDipindai: Int
)

data class NotifikasiPesananProduksiUi(
    val nomorPesanan: String,
    val pelanggan: String,
    val waktuPesanan: String,
    val daftarItem: List<ItemPersiapanProduksiUi>
)

@Composable
fun NotifikasiPesananProduksiScreen(
    daftarPesanan: List<NotifikasiPesananProduksiUi>,
    onBukaPersiapan: (NotifikasiPesananProduksiUi) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Pesanan dari Penjualan",
                color = AccessColors.Ink,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (daftarPesanan.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Text(
                        text = "Belum ada pesanan yang perlu disiapkan.",
                        modifier = Modifier.padding(18.dp),
                        color = AccessColors.Muted
                    )
                }
            }
        }

        items(daftarPesanan, key = { it.nomorPesanan }) { pesanan ->
            KartuPersiapanProduksi(
                pesanan = pesanan,
                onBuka = { onBukaPersiapan(pesanan) }
            )
        }
    }
}

@Composable
private fun KartuPersiapanProduksi(
    pesanan: NotifikasiPesananProduksiUi,
    onBuka: () -> Unit
) {
    val totalDiminta = pesanan.daftarItem.sumOf { it.jumlahPesanan }
    val totalDipindai = pesanan.daftarItem.sumOf { it.jumlahDipindai }

    // Setiap varian harus sesuai. Karung 5 kg tidak dapat
    // menggantikan kekurangan karung 25 kg dalam pesanan.
    val semuaSesuai = pesanan.daftarItem.isNotEmpty() &&
            pesanan.daftarItem.all {
                it.jumlahPesanan > 0 && it.jumlahDipindai == it.jumlahPesanan
            }

    val status = when {
        semuaSesuai -> "Siap dikonfirmasi"
        totalDipindai > 0 -> "Sedang disiapkan"
        else -> "Pesanan baru"
    }

    val teksTombol = when {
        semuaSesuai -> "Periksa pengiriman"
        totalDipindai > 0 -> "Lanjutkan persiapan"
        else -> "Siapkan pesanan"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, AccessColors.Border
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pesanan.pelanggan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = AccessColors.Ink
                    )
                    Text(
                        text = pesanan.nomorPesanan,
                        color = AccessColors.Muted,
                        fontSize = 13.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (totalDipindai == 0) {
                        Color(0xFFFFEFCB)
                    } else {
                        Color(0xFFE2F2E8)
                    }
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 5.dp
                        ),
                        color = if (totalDipindai == 0) {
                            Color(0xFF815300)
                        } else {
                            AccessColors.Green
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = pesanan.waktuPesanan,
                color = AccessColors.Muted,
                fontSize = 13.sp
            )

            HorizontalDivider(color = AccessColors.Border)

            pesanan.daftarItem.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${item.namaProduk} · ${item.kemasan}",
                        color = AccessColors.Ink,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${item.jumlahPesanan} karung dipesan",
                        color = AccessColors.Muted,
                        fontSize = 14.sp
                    )
                    if (pesanan.daftarItem.size > 1) {
                        Text(
                            text = "${item.jumlahDipindai} karung dipindai",
                            color = AccessColors.Green,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF0F5F1)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Pemindaian karung",
                        color = AccessColors.Muted,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "$totalDipindai dari $totalDiminta karung",
                        color = AccessColors.Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }

            Button(
                onClick = onBuka,
                enabled = totalDiminta > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccessColors.Green,
                    contentColor = Color.White
                )
            ) {
                Text(teksTombol)
            }
        }
    }
}

@Composable
private fun ContohNotifikasiProduksi(jumlahDipindai: Int) {
    val pesanan = NotifikasiPesananProduksiUi(
        nomorPesanan = "PS-001",
        pelanggan = "MBG A",
        waktuPesanan = "11 November 2026 · 08.15",
        daftarItem = listOf(
            ItemPersiapanProduksiUi(
                namaProduk = "Beras Premium",
                kemasan = "5 kg",
                jumlahPesanan = 50,
                jumlahDipindai = jumlahDipindai
            )
        )
    )

    SIRABASTheme {
        NotifikasiPesananProduksiScreen(
            daftarPesanan = listOf(pesanan),
            onBukaPersiapan = {}
        )
    }
}

@Preview(
    name = "Produksi · Pesanan baru",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PesananProduksiBaruPreview() {
    ContohNotifikasiProduksi(jumlahDipindai = 0)
}

@Preview(
    name = "Produksi · Sedang disiapkan",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PesananProduksiDiprosesPreview() {
    ContohNotifikasiProduksi(jumlahDipindai = 18)
}