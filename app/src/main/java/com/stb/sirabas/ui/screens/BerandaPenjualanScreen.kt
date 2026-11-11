package com.stb.sirabas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val formatAngka = remember {
        NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Beranda Penjualan",
                        color = AccessColors.Ink,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Selamat datang, $namaPetugas",
                        color = AccessColors.Muted
                    )
                }
            }

            item {
                GrafikHarianPenjualan(trenHarian)
            }

            item {
                JudulKatalogPenjualan("Hasil produksi", hasilProduksi.size)
            }

            items(hasilProduksi, key = { it.kode }) { produk ->
                KartuProdukKatalog(produk, keranjang, onTambah)
            }

            item {
                JudulKatalogPenjualan("Hasil samping", hasilSamping.size)
            }

            items(hasilSamping, key = { it.kode }) { produk ->
                KartuProdukKatalog(produk, keranjang, onTambah)
            }
        }

        // Ringkasan pesanan tetap terlihat saat katalog digulir.
        if (keranjang.isNotEmpty()) {
            val total = keranjang.fold(BigDecimal.ZERO) { jumlah, item ->
                jumlah + item.hargaSatuan.toBigDecimal() * item.jumlah.toBigDecimal()
            }
            val jumlahJenis = keranjang.distinctBy { it.produk.kode }.size

            Surface(
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$jumlahJenis jenis produk",
                            color = AccessColors.Muted,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Rp${formatAngka.format(total)}",
                            color = AccessColors.Ink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                    }

                    Button(
                        onClick = onCheckout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccessColors.Green,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Checkout")
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
    val sudahDipesan = keranjang
        .filter { it.produk.kode == produk.kode }
        .sumOf { it.jumlah }

    val sisaUntukDipesan = (produk.stokTersedia - sudahDipesan).coerceAtLeast(0)

    KartuProdukPenjualan(
        produk = produk.copy(stokTersedia = sisaUntukDipesan),
        onTambah = { _, jumlah, harga ->
            if (jumlah in 1..sisaUntukDipesan && harga > 0L) {
                onTambah(produk, jumlah, harga)
            }
        }
    )
}

@Composable
private fun JudulKatalogPenjualan(judul: String, jumlah: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = judul,
            color = AccessColors.Ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$jumlah produk",
            color = AccessColors.Muted,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun GrafikHarianPenjualan(data: List<PenjualanHarianUi>) {
    val harian = data.takeLast(7)
    val tertinggi = harian.maxOfOrNull { it.totalRupiah }?.coerceAtLeast(1L) ?: 1L
    val formatAngka = remember {
        NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF123F2E),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Tren penjualan harian",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            if (harian.isEmpty()) {
                Text("Belum ada data penjualan.", color = Color.White)
            } else {
                val hariTerakhir = harian.last()

                Text(
                    text = "${hariTerakhir.tanggal} · Rp${formatAngka.format(hariTerakhir.totalRupiah)}",
                    color = Color(0xFFE3CB91),
                    fontSize = 14.sp
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
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
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
                                            color = Color(0xFFA8D5AC),
                                            shape = RoundedCornerShape(
                                                topStart = 4.dp,
                                                topEnd = 4.dp
                                            )
                                        )
                                )
                            }

                            Text(
                                text = hari.tanggal.substringBefore(" "),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Penjualan · Beranda dan checkout",
    showBackground = true,
    widthDp = 390,
    heightDp = 900
)
@Composable
private fun BerandaPenjualanPreview() {
    val utama = listOf(
        ProdukPenjualanUi(
            kode = "PREMIUM-25-HIJAU",
            nama = "Beras Premium",
            kemasan = "25 kg hijau",
            satuanJual = "karung",
            stokTersedia = 12,
            hargaKatalog = 350_000L,
            fotoRes = R.drawable.premium_25kg_hijau
        ),
        ProdukPenjualanUi(
            "PREMIUM-5", "Beras Premium", "5 kg", "karung", 20, 75_000L
        ),
        ProdukPenjualanUi(
            "PREMIUM-10", "Beras Premium", "10 kg", "karung", 15, 145_000L
        ),
        ProdukPenjualanUi(
            "PREMIUM-25-BIRU", "Beras Premium", "25 kg biru muda", "karung", 8, 350_000L
        ),
        ProdukPenjualanUi(
            "MEDIUM-25", "Beras Medium", "25 kg", "karung", 10, 310_000L
        ),
        ProdukPenjualanUi(
            "BERAS-50", "Beras", "50 kg", "karung", 5, 600_000L
        )
    )

    val samping = listOf(
        "Dedak Halus", "Dedak Kasar", "Menir", "Reject", "Broken", "Sekam"
    ).map { nama ->
        ProdukPenjualanUi(
            kode = "SAMPING-${nama.uppercase(Locale.ROOT).replace(' ', '-')}",
            nama = nama,
            kemasan = "Curah",
            satuanJual = "kg",
            stokTersedia = 50,
            hargaKatalog = 3_000L
        )
    }

    val tren = listOf(
        PenjualanHarianUi("5 Nov", 700_000),
        PenjualanHarianUi("6 Nov", 1_050_000),
        PenjualanHarianUi("7 Nov", 850_000),
        PenjualanHarianUi("8 Nov", 1_400_000),
        PenjualanHarianUi("9 Nov", 1_100_000),
        PenjualanHarianUi("10 Nov", 1_750_000),
        PenjualanHarianUi("11 Nov", 1_400_000)
    )

    // Dua karung khusus Preview agar Checkout langsung terlihat.
    val keranjang = remember {
        mutableStateListOf(
            ItemPesananUi(
                produk = utama.first(),
                jumlah = 2,
                hargaSatuan = utama.first().hargaKatalog
            )
        )
    }
    var tampilInvoice by remember { mutableStateOf(false) }

    SIRABASTheme {
        BerandaPenjualanScreen(
            namaPetugas = "Rina",
            trenHarian = tren,
            hasilProduksi = utama,
            hasilSamping = samping,
            keranjang = keranjang.toList(),
            onTambah = { produk, jumlah, harga ->
                val jumlahDiKeranjang = keranjang
                    .filter { it.produk.kode == produk.kode }
                    .sumOf { it.jumlah }

                val sisaStok = produk.stokTersedia - jumlahDiKeranjang

                if (jumlah in 1..sisaStok && harga > 0L) {
                    val indeks = keranjang.indexOfFirst {
                        it.produk.kode == produk.kode && it.hargaSatuan == harga
                    }

                    if (indeks >= 0) {
                        val itemLama = keranjang[indeks]
                        keranjang[indeks] = itemLama.copy(
                            jumlah = itemLama.jumlah + jumlah
                        )
                    } else {
                        keranjang.add(
                            ItemPesananUi(
                                produk = produk,
                                jumlah = jumlah,
                                hargaSatuan = harga
                            )
                        )
                    }
                }
            },
            onCheckout = {
                tampilInvoice = keranjang.isNotEmpty()
            }
        )

        if (tampilInvoice) {
            CheckoutInvoiceDialog(
                namaPt = "PT Sri Tani Berkah",
                alamatPt = "",
                teleponPt = "",
                tanggalPesanan = "11 November 2026",
                keranjang = keranjang.toList(),
                daftarPelanggan = listOf("MBG A", "MBG B"),
                onTutup = {
                    tampilInvoice = false
                },
                onKonfirmasi = { _, _, _, _ ->
                    tampilInvoice = false
                }
            )
        }
    }
}