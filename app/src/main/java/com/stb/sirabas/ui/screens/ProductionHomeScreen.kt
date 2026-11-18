package com.stb.sirabas.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.R
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.text.NumberFormat
import java.util.Locale

data class ProductionHomeUi(
    val namaPetugas: String,
    val tanggal: String,
    val penerimaanHariIniKg: Double,
    val jumlahPenerimaan: Int,
    val stokBahanBakuKg: Double,
    val jumlahTugasLabel: Int,
    val drafPenerimaanKg: Double,
    val pesanan: List<ProductionOrderUi>
)

data class ProductionOrderUi(
    val nomorPesanan: String,
    val namaPelanggan: String,
    val namaProduk: String,
    val beratPerKarungKg: Int,
    val jumlahKarung: Int,
    val kemasan: String = "$beratPerKarungKg kg",
    val tanggalPesanan: String = ""
)

@Composable
fun ProductionHomeScreen(
    data: ProductionHomeUi? = null,
    batchProduksi: List<BatchProduksiUi> = emptyList(),
    hasilPindai: MutableMap<String, KarungDipindaiUi> =
        androidx.compose.runtime.remember {
            androidx.compose.runtime.mutableStateMapOf<String, KarungDipindaiUi>()
        },
    onPeriksaPengiriman: (NotifikasiPesananProduksiUi) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var bukaDaftarPesanan by rememberSaveable {
        mutableStateOf(false)
    }

    val posisiGulir = rememberScrollState()
    val warna = MaterialTheme.colorScheme

    if (bukaDaftarPesanan && data != null) {
        BackHandler {
            bukaDaftarPesanan = false
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = warna.background,
            contentColor = warna.onBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
            ) {
                TextButton(
                    onClick = {
                        bukaDaftarPesanan = false
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "‹ Beranda Produksi",
                        color = warna.primary
                    )
                }

                HorizontalDivider(
                    color = warna.outlineVariant
                )

                Box(modifier = Modifier.weight(1f)) {
                    PersiapanDenganPemindai(
                        daftarPesanan = data.pesanan
                            .groupBy { it.nomorPesanan }
                            .map { (nomor, daftarProduk) ->
                                val pertama = daftarProduk.first()

                                NotifikasiPesananProduksiUi(
                                    nomorPesanan = nomor,
                                    pelanggan = pertama.namaPelanggan,
                                    waktuPesanan = pertama.tanggalPesanan,
                                    daftarItem = daftarProduk
                                        .groupBy { it.namaProduk to it.kemasan }
                                        .map { (produk, baris) ->
                                            ItemPersiapanProduksiUi(
                                                namaProduk = produk.first,
                                                kemasan = produk.second,
                                                jumlahPesanan = baris.sumOf { it.jumlahKarung },
                                                jumlahDipindai = 0
                                            )
                                        }
                                )
                            },
                        batchProduksi = batchProduksi,
                        hasilPindai = hasilPindai,
                        onPeriksaPengiriman = onPeriksaPengiriman
                    )
                }
            }
        }

        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = warna.background,
        contentColor = warna.onBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .verticalScroll(posisiGulir)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Beranda Produksi",
                        color = warna.onBackground,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = data?.let {
                            "Selamat datang, ${it.namaPetugas}"
                        } ?: "Selamat datang",
                        color = warna.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    HorizontalDivider(
                        color = warna.outlineVariant,
                        thickness = 1.dp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_stb),
                        contentDescription = "Logo PT Sri Tani Berkah",
                        modifier = Modifier.size(
                            width = 80.dp,
                            height = 60.dp
                        )
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "SIRABAS",
                            color = warna.onBackground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "PT Sri Tani Berkah",
                            color = warna.onSurfaceVariant,
                            fontSize = 12.sp
                        )

                        Surface(
                            color = warna.secondaryContainer,
                            contentColor = warna.onSecondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Petugas Produksi",
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Kartu utama tetap hijau tua dengan teks terang.
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF123F2E),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "PENERIMAAN HARI INI",
                            color = Color(0xFFDCECAD),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (data != null) {
                            Text(
                                text = data.tanggal,
                                color = Color(0xFFDCECAD),
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = data?.let {
                                "${formatBerat(it.penerimaanHariIniKg)} kg"
                            } ?: "—",
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = data?.let {
                                "${it.jumlahPenerimaan} penerimaan tercatat"
                            } ?: "Data penerimaan belum tersedia",
                            color = Color(0xFFDCECAD),
                            fontSize = 13.sp
                        )

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.25f)
                        )

                        Text(
                            text = data?.let {
                                "Stok bahan baku tersedia: " +
                                        "${formatBerat(it.stokBahanBakuKg)} kg"
                            } ?: "Stok bahan baku: —",
                            color = Color(0xFFDCECAD),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProductionSummaryCard(
                        title = "Tugas label",
                        value = data?.jumlahTugasLabel?.toString() ?: "—",
                        description = "Label perlu ditindaklanjuti",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    ProductionSummaryCard(
                        title = "Draf penerimaan",
                        value = data?.let {
                            "${formatBerat(it.drafPenerimaanKg)} kg"
                        } ?: "—",
                        description = "Belum menambah stok",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Pesanan perlu disiapkan",
                        color = warna.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Pesanan dari bagian penjualan.",
                        color = warna.onSurfaceVariant,
                        fontSize = 13.sp
                    )

                    if (data != null && data.pesanan.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                bukaDaftarPesanan = true
                            },
                            contentPadding = PaddingValues(
                                horizontal = 0.dp
                            )
                        ) {
                            Text(
                                text = "Lihat daftar pesanan ›",
                                color = warna.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                when {
                    data == null -> {
                        AccessCard {
                            Text(
                                text = "Data pesanan belum tersedia",
                                color = warna.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    data.pesanan.isEmpty() -> {
                        AccessCard {
                            Text(
                                text = "Belum ada pesanan yang perlu disiapkan",
                                color = warna.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Pesanan baru akan ditampilkan di sini.",
                                color = warna.onSurfaceVariant,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    else -> {
                        data.pesanan.forEach { pesanan ->
                            ProductionOrderCard(pesanan)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ProductionSummaryCard(
    title: String,
    value: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val warna = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        color = warna.surface,
        contentColor = warna.onSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = warna.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = 130.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = warna.onSurfaceVariant,
                fontSize = 12.sp
            )

            Text(
                text = value,
                color = warna.onSurface,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                color = warna.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ProductionOrderCard(
    pesanan: ProductionOrderUi
) {
    val warna = MaterialTheme.colorScheme

    AccessCard {
        Surface(
            color = warna.tertiaryContainer,
            contentColor = warna.onTertiaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Menunggu persiapan",
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 6.dp
                ),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = pesanan.namaPelanggan,
            color = warna.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = pesanan.nomorPesanan,
            color = warna.onSurfaceVariant,
            fontSize = 12.sp
        )

        HorizontalDivider(
            color = warna.outlineVariant
        )

        Text(
            text = pesanan.namaProduk,
            color = warna.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "${pesanan.jumlahKarung} karung · ${pesanan.kemasan}",
            color = warna.onSurfaceVariant,
            fontSize = 14.sp
        )

        Text(
            text = "Pindai label setiap karung saat menyiapkan " +
                    "barang untuk pengiriman.",
            color = warna.primary,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}

private fun formatBerat(value: Double): String {
    return NumberFormat.getNumberInstance(
        Locale.forLanguageTag("id-ID")
    ).apply {
        maximumFractionDigits = 2
    }.format(value)
}

private fun contohBerandaProduksi(): ProductionHomeUi {
    return ProductionHomeUi(
        namaPetugas = "Dedi",
        tanggal = "18 November 2026",
        penerimaanHariIniKg = 1000.0,
        jumlahPenerimaan = 1,
        stokBahanBakuKg = 1500.0,
        jumlahTugasLabel = 1,
        drafPenerimaanKg = 200.0,
        pesanan = listOf(
            ProductionOrderUi(
                nomorPesanan = "PS-001",
                namaPelanggan = "MBG A",
                namaProduk = "Beras Premium",
                beratPerKarungKg = 25,
                jumlahKarung = 50
            )
        )
    )
}

@Preview(
    name = "Beranda Produksi · Terang",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun ProductionHomeScreenPreview() {
    SIRABASTheme(darkTheme = false) {
        ProductionHomeScreen(
            data = contohBerandaProduksi()
        )
    }
}

@Preview(
    name = "Beranda Produksi · Gelap",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun ProductionHomeScreenDarkPreview() {
    SIRABASTheme(darkTheme = true) {
        ProductionHomeScreen(
            data = contohBerandaProduksi()
        )
    }
}