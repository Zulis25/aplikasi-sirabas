package com.stb.sirabas.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
    val jumlahKarung: Int
)

@Composable
fun ProductionHomeScreen(
    data: ProductionHomeUi? = null
) {
    var bukaDaftarPesanan by rememberSaveable { mutableStateOf(false) }
    val posisiGulir = rememberScrollState()

    if (bukaDaftarPesanan && data != null) {
        BackHandler {
            bukaDaftarPesanan = false
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AccessColors.Background)
                .safeDrawingPadding()
        ) {
            TextButton(
                onClick = { bukaDaftarPesanan = false },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("‹  Beranda Produksi", color = AccessColors.Green)
            }

            Box(modifier = Modifier.weight(1f)) {
                AlurPersiapanPesananScreen(
                    daftarPesanan = data.pesanan.map { pesanan ->
                        NotifikasiPesananProduksiUi(
                            nomorPesanan = pesanan.nomorPesanan,
                            pelanggan = pesanan.namaPelanggan,
                            waktuPesanan = "",
                            daftarItem = listOf(
                                ItemPersiapanProduksiUi(
                                    namaProduk = pesanan.namaProduk,
                                    kemasan = "${pesanan.beratPerKarungKg} kg",
                                    jumlahPesanan = pesanan.jumlahKarung,
                                    jumlahDipindai = 0
                                )
                            )
                        )
                    },
                    onPindaiKarung = {},
                    onPeriksaPengiriman = {}
                )
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Beranda Produksi",
                    color = AccessColors.Ink,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = data?.let { "Selamat datang, ${it.namaPetugas}" }
                        ?: "Selamat datang",
                    color = AccessColors.Muted,
                    fontSize = 14.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_stb),
                    contentDescription = "Logo PT Sri Tani Berkah",
                    modifier = Modifier.size(width = 80.dp, height = 60.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SIRABAS",
                        color = AccessColors.Ink,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "PT Sri Tani Berkah",
                        color = AccessColors.Muted,
                        fontSize = 12.sp
                    )
                    Surface(
                        color = AccessColors.SoftGreen,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Petugas Produksi",
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),
                            color = AccessColors.Green,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF123F2E),
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

                    HorizontalDivider(color = Color.White.copy(alpha = 0.25f))

                    Text(
                        text = data?.let {
                            "Stok bahan baku tersedia: ${formatBerat(it.stokBahanBakuKg)} kg"
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
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                ProductionSummaryCard(
                    title = "Draf penerimaan",
                    value = data?.let {
                        "${formatBerat(it.drafPenerimaanKg)} kg"
                    } ?: "—",
                    description = "Belum menambah stok",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Pesanan perlu disiapkan",
                    color = AccessColors.Ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pesanan dari bagian penjualan.",
                    color = AccessColors.Muted,
                    fontSize = 13.sp
                )

                if (data != null && data.pesanan.isNotEmpty()) {
                    TextButton(
                        onClick = { bukaDaftarPesanan = true },
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) {
                        Text(
                            text = "Lihat daftar pesanan ›",
                            color = AccessColors.Green,
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
                            color = AccessColors.Ink,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                data.pesanan.isEmpty() -> {
                    AccessCard {
                        Text(
                            text = "Belum ada pesanan yang perlu disiapkan",
                            color = AccessColors.Ink,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Pesanan baru akan ditampilkan di sini.",
                            color = AccessColors.Muted,
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

@Composable
private fun ProductionSummaryCard(
    title: String,
    value: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AccessColors.Border)
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = 130.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, color = AccessColors.Muted, fontSize = 12.sp)
            Text(
                text = value,
                color = AccessColors.Ink,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = AccessColors.Muted,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ProductionOrderCard(pesanan: ProductionOrderUi) {
    AccessCard {
        Surface(
            color = AccessColors.SoftGold,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Menunggu persiapan",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                color = AccessColors.GoldInk,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = pesanan.namaPelanggan,
            color = AccessColors.Ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = pesanan.nomorPesanan,
            color = AccessColors.Muted,
            fontSize = 12.sp
        )

        HorizontalDivider(color = AccessColors.Border)

        Text(
            text = pesanan.namaProduk,
            color = AccessColors.Ink,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "${pesanan.jumlahKarung} karung · ${pesanan.beratPerKarungKg} kg/karung",
            color = AccessColors.Muted,
            fontSize = 14.sp
        )
        Text(
            text = "Pindai label setiap karung saat menyiapkan barang untuk pengiriman.",
            color = AccessColors.Green,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}

private fun formatBerat(value: Double): String {
    return NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 2
    }.format(value)
}

@Preview(
    name = "Beranda Produksi",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun ProductionHomeScreenPreview() {
    SIRABASTheme {
        ProductionHomeScreen(
            data = ProductionHomeUi(
                namaPetugas = "Dedi",
                tanggal = "11 November 2026",
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
        )
    }
}