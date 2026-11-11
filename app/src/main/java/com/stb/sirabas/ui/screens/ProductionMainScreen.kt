package com.stb.sirabas.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.R
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Locale

private enum class ProductionTab(
    val judul: String,
    val ikon: Int
) {
    BERANDA("Beranda", R.drawable.ic_nav_beranda),
    PRODUKSI("Produksi", R.drawable.ic_nav_produksi),
    STOK("Stok", R.drawable.ic_nav_stok),
    LAPORAN("Laporan", R.drawable.ic_nav_laporan),
    AKUN("Akun", R.drawable.ic_nav_akun)
}

@Composable
fun ProductionMainScreen(
    dataBeranda: ProductionHomeUi? = null,
    dataBahanBaku: List<BahanBakuUi>? = null
) {
    var tabTerpilih by rememberSaveable {
        mutableStateOf(ProductionTab.BERANDA.name)
    }

    val penyimpanState = rememberSaveableStateHolder()

    // Kedua daftar ini dipakai bersama oleh tab Beranda, Produksi, dan Stok.
    val penerimaanBaru = remember { mutableStateListOf<BahanBakuUi>() }
    val batchProduksi = remember { mutableStateListOf<BatchProduksiUi>() }

    val semuaPenerimaan = dataBahanBaku.orEmpty() + penerimaanBaru

    // Form produksi menerima sisa setiap lot setelah pemakaian sebelumnya.
    val lotSiapProduksi = semuaPenerimaan.mapNotNull { bahan ->
        val sisa = sisaLotKg(bahan, batchProduksi)

        if (sisa.signum() > 0) {
            bahan.copy(beratKg = sisa.toDouble())
        } else {
            null
        }
    }

    val tambahanStokKg = penerimaanBaru.sumOf { it.beratKg }
    val totalBahanTerpakai = batchProduksi.fold(BigDecimal.ZERO) { total, batch ->
        total + batch.beratInput
    }

    // Saldo awal beranda sudah mencakup penerimaan lama.
    // Jika saldo awal tidak diberikan, gunakan berat dari daftar lot awal.
    val stokAwalKg = dataBeranda?.stokBahanBakuKg
        ?: dataBahanBaku.orEmpty().sumOf { it.beratKg }

    val stokTerkiniKg = stokAwalKg + tambahanStokKg - totalBahanTerpakai.toDouble()

    val berandaTerbaru = dataBeranda?.let { awal ->
        val tanggalRingkasan = remember(awal.tanggal) {
            tanggalBerandaUntukProduksi(awal.tanggal)
        }

        val penerimaanPadaTanggalIni = penerimaanBaru.filter {
            it.tanggal == tanggalRingkasan
        }

        awal.copy(
            stokBahanBakuKg = stokTerkiniKg,
            penerimaanHariIniKg = awal.penerimaanHariIniKg +
                    penerimaanPadaTanggalIni.sumOf { it.beratKg },
            jumlahPenerimaan = awal.jumlahPenerimaan +
                    penerimaanPadaTanggalIni.size
        )
    }

    val nomorBahanTerbesar = semuaPenerimaan
        .mapNotNull { it.kode.removePrefix("BM/").toIntOrNull() }
        .maxOrNull() ?: 0

    val kodeBahanBerikutnya = "BM/" +
            (nomorBahanTerbesar + 1).toString().padStart(3, '0')

    BackHandler(enabled = tabTerpilih != ProductionTab.BERANDA.name) {
        tabTerpilih = ProductionTab.BERANDA.name
    }

    Scaffold(
        containerColor = AccessColors.Background,
        bottomBar = {
            Column {
                HorizontalDivider(
                    color = AccessColors.Border,
                    thickness = 1.dp
                )

                NavigationBar(
                    containerColor = Color.White,
                    contentColor = AccessColors.Green,
                    tonalElevation = 0.dp
                ) {
                    ProductionTab.values().forEach { tab ->
                        val aktif = tabTerpilih == tab.name

                        NavigationBarItem(
                            selected = aktif,
                            onClick = { tabTerpilih = tab.name },
                            icon = {
                                Icon(
                                    painter = painterResource(tab.ikon),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.judul,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = if (aktif) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccessColors.Green,
                                selectedTextColor = AccessColors.Green,
                                indicatorColor = AccessColors.SoftGreen,
                                unselectedIconColor = AccessColors.Muted,
                                unselectedTextColor = AccessColors.Muted
                            )
                        )
                    }
                }
            }
        }
    ) { paddingHalaman ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingHalaman)
                .consumeWindowInsets(paddingHalaman)
        ) {
            penyimpanState.SaveableStateProvider(key = tabTerpilih) {
                when (tabTerpilih) {
                    ProductionTab.BERANDA.name -> {
                        ProductionHomeScreen(data = berandaTerbaru)
                    }

                    ProductionTab.PRODUKSI.name -> {
                        var halamanProduksi by rememberSaveable {
                            mutableStateOf("daftar")
                        }

                        when (halamanProduksi) {
                            "tambah_bahan" -> {
                                FormBahanBakuScreen(
                                    kodeBaru = kodeBahanBerikutnya,
                                    onKembali = {
                                        halamanProduksi = "daftar"
                                    },
                                    onSimpan = { bahan ->
                                        if (halamanProduksi == "tambah_bahan") {
                                            penerimaanBaru.add(bahan)
                                            halamanProduksi = "daftar"
                                        }
                                    }
                                )
                            }

                            "hasil_produksi" -> {
                                HitungProduksiScreen(
                                    bahanTersedia = lotSiapProduksi,
                                    onKembali = {
                                        halamanProduksi = "daftar"
                                    },
                                    onKonfirmasi = simpan@{ isian ->
                                        // Abaikan ketukan kedua setelah batch disimpan.
                                        if (halamanProduksi != "hasil_produksi") {
                                            return@simpan
                                        }

                                        val bahan = semuaPenerimaan.firstOrNull {
                                            it.kode == isian.kodeBahan
                                        } ?: return@simpan

                                        val sisaLot = sisaLotKg(bahan, batchProduksi)
                                        val totalHasil = isian.beratUtama +
                                                isian.beratSamping

                                        if (
                                            isian.beratInput.signum() <= 0 ||
                                            isian.beratInput > sisaLot ||
                                            isian.beratUtama.signum() < 0 ||
                                            isian.beratSamping.signum() < 0 ||
                                            totalHasil.signum() <= 0 ||
                                            totalHasil > isian.beratInput
                                        ) {
                                            return@simpan
                                        }

                                        val nomorBatch = batchProduksi.size + 1
                                        val kodeBatch = "PR/" +
                                                nomorBatch.toString().padStart(3, '0')

                                        batchProduksi.add(
                                            isian.copy(kode = kodeBatch)
                                        )

                                        halamanProduksi = "daftar"
                                    }
                                )
                            }

                            else -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    OutlinedButton(
                                        onClick = {
                                            halamanProduksi = "hasil_produksi"
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = 20.dp,
                                                vertical = 8.dp
                                            )
                                    ) {
                                        Text(
                                            text = "Hasil Produksi",
                                            color = AccessColors.Green
                                        )
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        BahanBakuScreen(
                                            data = if (
                                                dataBahanBaku == null &&
                                                penerimaanBaru.isEmpty()
                                            ) {
                                                null
                                            } else {
                                                semuaPenerimaan
                                            },
                                            onTambah = {
                                                halamanProduksi = "tambah_bahan"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ProductionTab.STOK.name -> {
                        StokBahanBakuScreen(
                            stokTersediaKg = stokTerkiniKg,
                            penerimaanBaru = penerimaanBaru,
                            batchProduksi = batchProduksi
                        )
                    }

                    ProductionTab.LAPORAN.name -> {
                        ProductionSectionScreen(
                            judul = "Laporan",
                            deskripsi = "Lihat laporan bahan baku, hasil produksi, dan stok gudang."
                        )
                    }

                    ProductionTab.AKUN.name -> {
                        ProductionSectionScreen(
                            judul = "Akun",
                            deskripsi = "Kelola profil dan pengaturan akun Anda."
                        )
                    }
                }
            }
        }
    }
}

private fun tanggalBerandaUntukProduksi(tanggal: String): String? {
    val teks = tanggal.trim()

    // Form penerimaan menggunakan yyyy-MM-dd.
    if (Regex("""\d{4}-\d{2}-\d{2}""").matches(teks)) {
        return teks
    }

    // Beranda dapat menggunakan format seperti "11 November 2026".
    val formatBeranda = SimpleDateFormat(
        "d MMMM yyyy",
        Locale.forLanguageTag("id-ID")
    ).apply {
        isLenient = false
    }

    val formatData = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

    return runCatching {
        formatBeranda.parse(teks)?.let { formatData.format(it) }
    }.getOrNull()
}

@Composable
private fun ProductionSectionScreen(
    judul: String,
    deskripsi: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = judul,
                color = AccessColors.Ink,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = deskripsi,
                color = AccessColors.Muted,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Preview(
    name = "Navigasi Petugas Produksi",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun ProductionMainScreenPreview() {
    SIRABASTheme {
        ProductionMainScreen(
            dataBahanBaku = contohBahanBaku(),
            dataBeranda = ProductionHomeUi(
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