package com.stb.sirabas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.R
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

data class PenjualanHarianUi(
    val tanggal: String,
    val totalRupiah: Long
)

data class ItemPesananUi(
    val produk: ProdukPenjualanUi,
    val jumlah: Int,
    val hargaSatuan: Long
)

private const val SEMUA = "Semua"
private const val PRODUKSI = "Hasil produksi"
private const val SAMPING = "Hasil samping"

private fun fotoProdukBeranda(kode: String): Int? = when (kode) {
    "PREMIUM-5" -> R.drawable.premium_5kg
    "PREMIUM-10" -> R.drawable.premium_10kg
    "PREMIUM-25-HIJAU" -> R.drawable.premium_25kg_hijau
    "PREMIUM-25-BIRU" -> R.drawable.premium_25kg_biru
    "MEDIUM-25" -> R.drawable.medium_25kg
    "BERAS-50" -> R.drawable.beras_50kg
    else -> null
}

@Composable
fun BerandaPenjualanScreen(
    namaPetugas: String,
    trenHarian: List<PenjualanHarianUi>,
    hasilProduksi: List<ProdukPenjualanUi>,
    hasilSamping: List<ProdukPenjualanUi>,
    keranjang: List<ItemPesananUi>,
    onTambah: (ProdukPenjualanUi, Int, Long) -> Unit,
    onCheckout: () -> Unit
) {
    var kategori by rememberSaveable { mutableStateOf(SEMUA) }
    val warna = MaterialTheme.colorScheme
    val formatAngka = remember {
        NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = warna.background,
        contentColor = warna.onBackground
    ) {
        Column {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Beranda Penjualan",
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selamat datang, $namaPetugas",
                            color = warna.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = warna.outlineVariant)
                    }
                }

                item {
                    GrafikHarianPenjualan(trenHarian, formatAngka)
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(SEMUA, PRODUKSI, SAMPING).forEach { pilihan ->
                            FilterChip(
                                selected = kategori == pilihan,
                                onClick = { kategori = pilihan },
                                label = { Text(pilihan) }
                            )
                        }
                    }
                }

                val kelompok = listOf(
                    PRODUKSI to hasilProduksi,
                    SAMPING to hasilSamping
                )

                kelompok.forEach { (judul, produkDalamKelompok) ->
                    if (kategori == SEMUA || kategori == judul) {
                        item(key = "judul-$judul") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = judul,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${produkDalamKelompok.size} produk",
                                    color = warna.onSurfaceVariant
                                )
                            }
                        }

                        if (produkDalamKelompok.isEmpty()) {
                            item(key = "kosong-$judul") {
                                Text(
                                    text = "Belum ada produk dalam kategori ini.",
                                    color = warna.onSurfaceVariant
                                )
                            }
                        }

                        items(
                            items = produkDalamKelompok,
                            key = { produk -> "$judul-${produk.kode}" }
                        ) { produk ->
                            KartuProdukKatalog(produk, keranjang, onTambah)
                        }
                    }
                }
            }

            if (keranjang.isNotEmpty()) {
                val total = keranjang.fold(BigDecimal.ZERO) { subtotal, item ->
                    subtotal + item.hargaSatuan.toBigDecimal() *
                            item.jumlah.toBigDecimal()
                }
                val jumlahJenis = keranjang.distinctBy { it.produk.kode }.size

                Surface(
                    color = warna.surface,
                    contentColor = warna.onSurface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$jumlahJenis jenis produk",
                                color = warna.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Rp${formatAngka.format(total)}",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(onClick = onCheckout) {
                            Text("Checkout")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KartuProdukKatalog(
    produk: ProdukPenjualanUi,
    keranjang: List<ItemPesananUi>,
    onTambah: (ProdukPenjualanUi, Int, Long) -> Unit
) {
    val jumlahDiKeranjang = keranjang
        .filter { it.produk.kode == produk.kode }
        .sumOf { it.jumlah }

    val sisa = (produk.stokTersedia - jumlahDiKeranjang).coerceAtLeast(0)
    val produkDenganFoto = produk.copy(
        fotoRes = if (produk.kode.startsWith("SAMPING-")) {
            R.drawable.beras_50kg
        } else {
            produk.fotoRes ?: fotoProdukBeranda(produk.kode)
        }
    )

    KartuProdukPenjualan(
        produk = produkDenganFoto.copy(stokTersedia = sisa),
        onTambah = { _, jumlah, harga ->
            if (jumlah in 1..sisa && harga > 0L) {
                onTambah(produkDenganFoto, jumlah, harga)
            }
        }
    )
}

@Composable
private fun GrafikHarianPenjualan(
    data: List<PenjualanHarianUi>,
    formatAngka: NumberFormat
) {
    val harian = data.takeLast(7)
    val tertinggi = (harian.maxOfOrNull { it.totalRupiah } ?: 0L)
        .coerceAtLeast(1L)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF123F2E),
        contentColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Tren penjualan harian",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            val terakhir = harian.lastOrNull()
            if (terakhir == null) {
                Text("Belum ada data penjualan.")
            } else {
                Text(
                    text = "${terakhir.tanggal} · Rp${formatAngka.format(terakhir.totalRupiah)}",
                    color = Color(0xFFE3CB91)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    harian.forEach { hari ->
                        val proporsi = (hari.totalRupiah.toDouble() / tertinggi)
                            .coerceIn(0.0, 1.0)
                            .toFloat()

                        Column(
                            modifier = Modifier.weight(1f).semantics {
                                contentDescription =
                                    "${hari.tanggal}, Rp${formatAngka.format(hari.totalRupiah)}"
                            },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier.height(64.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .height(64.dp * proporsi)
                                        .background(
                                            Color(0xFFA8D5AC),
                                            RoundedCornerShape(
                                                topStart = 4.dp,
                                                topEnd = 4.dp
                                            )
                                        )
                                )
                            }

                            Text(
                                text = hari.tanggal.substringBefore(" "),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun BerandaPenjualanPreview() {
    SIRABASTheme {
        BerandaPenjualanScreen(
            namaPetugas = "Rina",
            trenHarian = listOf(
                PenjualanHarianUi("10 Nov", 750_000L),
                PenjualanHarianUi("11 Nov", 950_000L)
            ),
            hasilProduksi = listOf(
                ProdukPenjualanUi(
                    "PREMIUM-5", "Beras Premium", "5 kg",
                    "karung", 20, 75_000L
                )
            ),
            hasilSamping = listOf(
                ProdukPenjualanUi(
                    "SAMPING-REJECT", "Reject", "Curah",
                    "kg", 50, 3_000L
                )
            ),
            keranjang = emptyList(),
            onTambah = { _, _, _ -> },
            onCheckout = {}
        )
    }
}