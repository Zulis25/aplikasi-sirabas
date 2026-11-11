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
fun PersiapanPesananScreen(
    pesanan: NotifikasiPesananProduksiUi,
    onKembali: () -> Unit,
    onPindaiKarung: () -> Unit,
    onPeriksaPengiriman: () -> Unit
) {
    val totalDiminta = pesanan.daftarItem.sumOf { it.jumlahPesanan }
    val totalDipindai = pesanan.daftarItem.sumOf { it.jumlahDipindai }

    // Periksa setiap kemasan agar kelebihan satu varian tidak
    // menutupi kekurangan varian lain.
    val semuaSesuai = pesanan.daftarItem.isNotEmpty() &&
            pesanan.daftarItem.all { item ->
                item.jumlahPesanan > 0 &&
                        item.jumlahDipindai == item.jumlahPesanan
            }

    val adaKekurangan = pesanan.daftarItem.any {
        it.jumlahDipindai < it.jumlahPesanan
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TextButton(
                onClick = onKembali,
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                Text("‹  Daftar pesanan", color = AccessColors.Green)
            }

            Text(
                text = "Persiapan pesanan",
                color = AccessColors.Ink,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF123F2E),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = pesanan.nomorPesanan,
                        color = Color(0xFFE3CB91),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = pesanan.pelanggan,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(pesanan.waktuPesanan, fontSize = 14.sp)

                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
                    Spacer(Modifier.height(6.dp))

                    Text("Karung sudah dipindai", fontSize = 14.sp)
                    Text(
                        text = "$totalDipindai dari $totalDiminta karung",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Produk yang disiapkan",
                color = AccessColors.Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            pesanan.daftarItem.forEach { item ->
                val selisih = item.jumlahPesanan - item.jumlahDipindai
                val sesuai = item.jumlahPesanan > 0 && selisih == 0

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, AccessColors.Border
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${item.namaProduk} · ${item.kemasan}",
                            color = AccessColors.Ink,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        BarisJumlahPersiapan("Dipesan", item.jumlahPesanan)
                        BarisJumlahPersiapan("Dipindai", item.jumlahDipindai)

                        HorizontalDivider(color = AccessColors.Border)

                        Text(
                            text = when {
                                sesuai -> "Jumlah sudah sesuai"
                                selisih > 0 -> "Masih kurang $selisih karung"
                                selisih < 0 -> "Periksa kelebihan ${-selisih} karung"
                                else -> "Periksa jumlah pesanan"
                            },
                            color = if (sesuai) {
                                AccessColors.Green
                            } else {
                                Color(0xFF815300)
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Tombol selalu terlihat meskipun daftar produk panjang.
        Surface(color = Color.White, shadowElevation = 3.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        if (semuaSesuai) {
                            onPeriksaPengiriman()
                        } else {
                            onPindaiKarung()
                        }
                    },
                    enabled = semuaSesuai || adaKekurangan,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccessColors.Green,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (semuaSesuai) {
                            "Periksa pengiriman"
                        } else {
                            "Pindai karung"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BarisJumlahPersiapan(label: String, jumlah: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AccessColors.Muted)
        Text(
            text = "$jumlah karung",
            color = AccessColors.Ink,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ContohPersiapanPesanan(jumlahDipindai: Int) {
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
        PersiapanPesananScreen(
            pesanan = pesanan,
            onKembali = {},
            onPindaiKarung = {},
            onPeriksaPengiriman = {}
        )
    }
}

@Preview(
    name = "Persiapan · Masih kurang 32 karung",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PersiapanSebagianPreview() {
    ContohPersiapanPesanan(jumlahDipindai = 18)
}

@Preview(
    name = "Persiapan · Semua karung sesuai",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PersiapanLengkapPreview() {
    ContohPersiapanPesanan(jumlahDipindai = 50)
}