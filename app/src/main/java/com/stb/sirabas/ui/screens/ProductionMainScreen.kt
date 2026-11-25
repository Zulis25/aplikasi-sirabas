package com.stb.sirabas.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import java.util.Date
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
    dataBahanBaku: List<BahanBakuUi>? = null,
    dataBatch: List<BatchProduksiUi>? = null,
    onSimpanBahanBaku: ((BahanBakuUi) -> Unit)? = null,
    onSimpanBatch: ((BatchProduksiUi) -> Unit)? = null,
    usernamePanduan: String = "",
    halamanAkun: @Composable () -> Unit = {
        ProductionSectionScreen(
            judul = "Akun",
            deskripsi = "Kelola profil dan pengaturan akun Anda."
        )
    }
) {
    val warna = MaterialTheme.colorScheme
    val context = LocalContext.current

    var tabTerpilih by rememberSaveable {
        mutableStateOf(ProductionTab.BERANDA.name)
    }

    var halamanProduksi by rememberSaveable {
        mutableStateOf("menu")
    }

    val penyimpanState = rememberSaveableStateHolder()

    // Data bersama untuk tab Produksi, Beranda, dan Stok.
    val penerimaanBaru = remember(dataBahanBaku) {
        mutableStateListOf<BahanBakuUi>().apply {
            if (dataBahanBaku != null) addAll(dataBahanBaku)
        }
    }

    val batchProduksi = remember(dataBatch) {
        mutableStateListOf<BatchProduksiUi>().apply {
            if (dataBatch != null) addAll(dataBatch)
        }
    }

    val hasilPindai = remember {
        androidx.compose.runtime.mutableStateMapOf<String, KarungDipindaiUi>()
    }


    val pengirimanTercatat = remember {
        androidx.compose.runtime.mutableStateMapOf<String, PengirimanKarungUi>()
    }

    var pesananDiperiksa by remember {
        mutableStateOf<NotifikasiPesananProduksiUi?>(null)
    }

    var pesanPengiriman by remember {
        mutableStateOf<String?>(null)
    }

    val kodeSudahKeluar = pengirimanTercatat.values
        .flatMap { it.kodeKarung }
        .toSet()

    pesananDiperiksa?.let { pesanan ->
        val karungPesanan = hasilPindai.filterValues {
            it.nomorPesanan == pesanan.nomorPesanan
        }

        DialogPeriksaKarung(
            pesanan = pesanan,
            karungPesanan = karungPesanan,
            onKembali = { pesananDiperiksa = null },
            onKonfirmasi = {
                val nomor = pesanan.nomorPesanan
                val kode = karungPesanan.keys.sorted()

                if (nomor !in pengirimanTercatat &&
                    kode.none { it in kodeSudahKeluar }
                ) {
                    pengirimanTercatat[nomor] = PengirimanKarungUi(
                        nomorPesanan = nomor,
                        pelanggan = pesanan.pelanggan,
                        tanggalKeluar = tanggalKeluarHariIni(),
                        kodeKarung = kode
                    )
                    pesanPengiriman = "$nomor · ${kode.size} karung tercatat keluar."
                }
                pesananDiperiksa = null
            }
        )
    }

    pesanPengiriman?.let { isi ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pesanPengiriman = null },
            title = { androidx.compose.material3.Text("Pengiriman tercatat") },
            text = { androidx.compose.material3.Text(isi) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { pesanPengiriman = null }
                ) {
                    androidx.compose.material3.Text("Selesai")
                }
            }
        )
    }

    val daftarRetur = remember {
        mutableStateListOf<ProdukReturUi>()
    }

    val semuaPenerimaan = dataBahanBaku.orEmpty() + penerimaanBaru

    val lotSiapProduksi = semuaPenerimaan.mapNotNull { bahan ->
        val sisa = sisaLotKg(bahan, batchProduksi)

        if (sisa.signum() > 0) {
            bahan.copy(beratKg = sisa.toDouble())
        } else {
            null
        }
    }

    val totalBahanTerpakai = batchProduksi.fold(
        BigDecimal.ZERO
    ) { jumlah, batch ->
        jumlah + batch.beratInput
    }

    val stokAwalKg = dataBeranda?.stokBahanBakuKg
        ?: dataBahanBaku.orEmpty().sumOf { it.beratKg }

    val stokTerkiniKg = stokAwalKg +
            penerimaanBaru.sumOf { it.beratKg } -
            totalBahanTerpakai.toDouble()

    val berandaTerbaru = dataBeranda?.let { awal ->
        val tanggalRingkasan = tanggalBerandaUntukProduksi(
            awal.tanggal
        )

        val penerimaanHariIni = penerimaanBaru.filter {
            it.tanggal == tanggalRingkasan
        }

        awal.copy(
            stokBahanBakuKg = stokTerkiniKg,
            penerimaanHariIniKg = awal.penerimaanHariIniKg +
                    penerimaanHariIni.sumOf { it.beratKg },
            jumlahPenerimaan = awal.jumlahPenerimaan +
                    penerimaanHariIni.size
        )
    }

    val nomorBahanTerbesar = semuaPenerimaan
        .mapNotNull {
            it.kode.removePrefix("BM/").toIntOrNull()
        }
        .maxOrNull() ?: 0

    val kodeBahanBerikutnya = "BM/" +
            (nomorBahanTerbesar + 1).toString().padStart(3, '0')

    val kodeReturBerikutnya = "RT/" +
            (daftarRetur.size + 1).toString().padStart(3, '0')

    val pilihanProdukRetur = listOf(
        "Premium" to "5 kg",
        "Premium" to "10 kg",
        "Premium" to "25 kg hijau",
        "Premium" to "25 kg biru",
        "Medium" to "25 kg",
        "Beras" to "50 kg"
    )

    fun kembaliDariProduksi() {
        halamanProduksi = when (halamanProduksi) {
            "tambah_bahan" -> "bahan"
            "tambah_retur" -> "retur"
            else -> "menu"
        }
    }

    BackHandler(
        enabled = tabTerpilih != ProductionTab.BERANDA.name
    ) {
        if (
            tabTerpilih == ProductionTab.PRODUKSI.name &&
            halamanProduksi != "menu"
        ) {
            kembaliDariProduksi()
        } else {
            tabTerpilih = ProductionTab.BERANDA.name
        }
    }

    Scaffold(
        containerColor = warna.background,
        contentColor = warna.onBackground,
        bottomBar = {
            val tabProduksi = ProductionTab.values()

            val menuPanduan = tabProduksi.map { tab ->
                MenuPanduanSirabas(
                    judul = tab.judul,
                    ikon = tab.ikon,
                    penjelasan = when (tab) {
                        ProductionTab.BERANDA ->
                            "Pantau penerimaan hari ini dan buka pesanan dari Penjualan yang perlu disiapkan."

                        ProductionTab.PRODUKSI ->
                            "Di sini kamu mencatat bahan baku masuk, hasil produksi, dan retur."

                        ProductionTab.STOK ->
                            "Lihat persediaan hasil utama per kemasan, hasil samping, dan riwayat produksi."

                        ProductionTab.LAPORAN ->
                            "Pilih jenis dan periode laporan produksi, lalu unduh hasilnya dalam format Excel."

                        ProductionTab.AKUN ->
                            "Edit profil, atur mode gelap, lihat informasi SIRABAS, atau keluar dari akun."
                    }
                )
            }

            NavigasiPanduanSirabas(
                username = usernamePanduan,
                peran = "produksi",
                daftarMenu = menuPanduan,
                indeksAktif = tabProduksi.indexOfFirst { it.name == tabTerpilih },
                onPilihMenu = { indeks ->
                    val tab = tabProduksi[indeks]
                    if (tab == ProductionTab.PRODUKSI) {
                        halamanProduksi = "menu"
                    }
                    tabTerpilih = tab.name
                }
            )
        }
    ) { paddingHalaman ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingHalaman)
                .consumeWindowInsets(paddingHalaman),
            color = warna.background,
            contentColor = warna.onBackground
        ) {
            penyimpanState.SaveableStateProvider(
                key = tabTerpilih
            ) {
                when (tabTerpilih) {
                    ProductionTab.BERANDA.name -> {
                        ProductionHomeScreen(
                            data = berandaTerbaru?.let { beranda ->
                                beranda.copy(
                                    pesanan = beranda.pesanan.filterNot { pesanan ->
                                        pesanan.nomorPesanan in pengirimanTercatat
                                    }
                                )
                            },
                            batchProduksi = batchProduksi,
                            hasilPindai = hasilPindai,
                            onPeriksaPengiriman = { pesanan ->
                                pesananDiperiksa = pesanan
                            }
                        )
                    }

                    ProductionTab.PRODUKSI.name -> {
                        when (halamanProduksi) {
                            "menu" -> {
                                MenuUtamaProduksi(
                                    onBahanMasuk = {
                                        halamanProduksi = "bahan"
                                    },
                                    onHasilProduksi = {
                                        halamanProduksi = "hasil"
                                    },
                                    onRetur = {
                                        halamanProduksi = "retur"
                                    }
                                )
                            }

                            "bahan" -> {
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    TextButton(
                                        onClick = {
                                            halamanProduksi = "menu"
                                        },
                                        modifier = Modifier.padding(
                                            start = 8.dp
                                        )
                                    ) {
                                        Text("‹ Menu Produksi")
                                    }

                                    HorizontalDivider(
                                        color = warna.outlineVariant
                                    )

                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        BahanBakuScreen(
                                            data = semuaPenerimaan,
                                            onTambah = {
                                                halamanProduksi =
                                                    "tambah_bahan"
                                            }
                                        )
                                    }
                                }
                            }

                            "tambah_bahan" -> {
                                FormBahanBakuScreen(
                                    kodeBaru = kodeBahanBerikutnya,
                                    onKembali = {
                                        halamanProduksi = "bahan"
                                    },
                                    onSimpan = { bahan ->
                                        if (
                                            halamanProduksi ==
                                            "tambah_bahan"
                                        ) {
                                            if (onSimpanBahanBaku != null) {
                                                onSimpanBahanBaku(bahan)
                                            } else {
                                                penerimaanBaru.add(bahan)
                                            }
                                            halamanProduksi = "bahan"
                                        }
                                    }
                                )
                            }

                            "hasil" -> {
                                HitungProduksiScreen(
                                    bahanTersedia = lotSiapProduksi,
                                    onKembali = {
                                        halamanProduksi = "menu"
                                    },
                                    onKonfirmasi = simpan@{ isian ->
                                        if (
                                            halamanProduksi != "hasil"
                                        ) {
                                            return@simpan
                                        }

                                        val bahan =
                                            semuaPenerimaan.firstOrNull {
                                                it.kode == isian.kodeBahan
                                            }

                                        if (bahan == null) {
                                            Toast.makeText(
                                                context,
                                                "Lot bahan tidak ditemukan.",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            return@simpan
                                        }

                                        val sisaLot = sisaLotKg(
                                            bahan,
                                            batchProduksi
                                        )

                                        val totalHasil =
                                            isian.beratUtama +
                                                    isian.beratSamping

                                        val tidakValid =
                                            isian.beratInput.signum() <= 0 ||
                                                    isian.beratInput > sisaLot ||
                                                    isian.beratUtama.signum() < 0 ||
                                                    isian.beratSamping.signum() < 0 ||
                                                    totalHasil.signum() <= 0 ||
                                                    totalHasil > isian.beratInput

                                        if (tidakValid) {
                                            Toast.makeText(
                                                context,
                                                "Periksa sisa bahan dan berat hasil produksi.",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            return@simpan
                                        }

                                        val nomorTerbesar = batchProduksi
                                            .mapNotNull {
                                                it.kode
                                                    .removePrefix("PR/")
                                                    .toIntOrNull()
                                            }
                                            .maxOrNull() ?: 0

                                        val kodeBatch = "PR/" +
                                                (nomorTerbesar + 1)
                                                    .toString()
                                                    .padStart(3, '0')

                                        val batchBaru = isian.copy(
                                            kode = kodeBatch
                                        )
                                        if (onSimpanBatch != null) {
                                            onSimpanBatch(batchBaru)
                                        } else {
                                            batchProduksi.add(batchBaru)
                                        }

                                        halamanProduksi = "menu"

                                        Toast.makeText(
                                            context,
                                            "Hasil produksi tersimpan.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }

                            "retur" -> {
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    TextButton(
                                        onClick = {
                                            halamanProduksi = "menu"
                                        },
                                        modifier = Modifier.padding(
                                            start = 8.dp
                                        )
                                    ) {
                                        Text("‹ Menu Produksi")
                                    }

                                    HorizontalDivider(
                                        color = warna.outlineVariant
                                    )

                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        ProdukReturScreen(
                                            daftarRetur = daftarRetur,
                                            onTambahRetur = {
                                                halamanProduksi =
                                                    "tambah_retur"
                                            }
                                        )
                                    }
                                }
                            }

                            "tambah_retur" -> {
                                FormProdukReturScreen(
                                    kodeRetur = kodeReturBerikutnya,
                                    tanggal = SimpleDateFormat(
                                        "yyyy-MM-dd",
                                        Locale.ROOT
                                    ).format(Date()),
                                    pilihanProduk = pilihanProdukRetur,
                                    onKembali = {
                                        halamanProduksi = "retur"
                                    },
                                    onSimpan = { retur ->
                                        if (
                                            halamanProduksi ==
                                            "tambah_retur"
                                        ) {
                                            daftarRetur.add(retur)
                                            halamanProduksi = "retur"
                                        }
                                    }
                                )
                            }
                        }
                    }

                    ProductionTab.STOK.name -> {
                        StokBahanBakuScreen(
                            stokTersediaKg = stokTerkiniKg,
                            penerimaanBaru = semuaPenerimaan,
                            batchProduksi = batchProduksi,
                            kodeSudahKeluar = kodeSudahKeluar
                        )
                    }

                    ProductionTab.LAPORAN.name -> {
                        LaporanProduksiDenganExcel(
                            penerimaan = semuaPenerimaan,
                            batchProduksi = batchProduksi
                        )
                    }

                    ProductionTab.AKUN.name -> {
                        halamanAkun()
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuUtamaProduksi(
    onBahanMasuk: () -> Unit,
    onHasilProduksi: () -> Unit,
    onRetur: () -> Unit
) {
    val warna = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Produksi",
            color = warna.onBackground,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Pilih kegiatan yang akan dikerjakan.",
            color = warna.onSurfaceVariant,
            fontSize = 14.sp
        )

        HorizontalDivider(
            color = warna.outlineVariant
        )

        KartuMenuUtamaProduksi(
            judul = "Bahan Baku Masuk",
            deskripsi = "Catat penerimaan beras atau gabah dari pemasok.",
            onClick = onBahanMasuk
        )

        KartuMenuUtamaProduksi(
            judul = "Hasil Produksi",
            deskripsi = "Catat pemakaian bahan, hasil utama, hasil samping, dan susut.",
            onClick = onHasilProduksi
        )

        KartuMenuUtamaProduksi(
            judul = "Retur",
            deskripsi = "Catat produk yang dikembalikan pelanggan.",
            onClick = onRetur
        )
    }
}

@Composable
private fun KartuMenuUtamaProduksi(
    judul: String,
    deskripsi: String,
    onClick: () -> Unit
) {
    val warna = MaterialTheme.colorScheme

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = warna.surface,
            contentColor = warna.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = judul,
                color = warna.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = deskripsi,
                color = warna.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )

            Text(
                text = "Buka ›",
                color = warna.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun tanggalBerandaUntukProduksi(
    tanggal: String
): String? {
    val teks = tanggal.trim()

    if (Regex("""\d{4}-\d{2}-\d{2}""").matches(teks)) {
        return teks
    }

    val formatBeranda = SimpleDateFormat(
        "d MMMM yyyy",
        Locale.forLanguageTag("id-ID")
    ).apply {
        isLenient = false
    }

    val formatData = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.ROOT
    )

    return runCatching {
        formatBeranda.parse(teks)?.let {
            formatData.format(it)
        }
    }.getOrNull()
}

@Composable
private fun ProductionSectionScreen(
    judul: String,
    deskripsi: String
) {
    val warna = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = judul,
            color = warna.onBackground,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        HorizontalDivider(
            color = warna.outlineVariant
        )

        Text(
            text = deskripsi,
            color = warna.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
    }
}

@Preview(
    name = "Navigasi Produksi · Terang",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun ProductionMainScreenPreview() {
    SIRABASTheme(darkTheme = false) {
        ContohNavigasiProduksi()
    }
}

@Preview(
    name = "Navigasi Produksi · Gelap",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun ProductionMainScreenDarkPreview() {
    SIRABASTheme(darkTheme = true) {
        ContohNavigasiProduksi()
    }
}

@Composable
private fun ContohNavigasiProduksi() {
    val bahan = contohBahanBaku()

    ProductionMainScreen(
        dataBahanBaku = bahan,
        dataBeranda = ProductionHomeUi(
            namaPetugas = "Dedi",
            tanggal = "18 November 2026",
            penerimaanHariIniKg = 0.0,
            jumlahPenerimaan = 0,
            stokBahanBakuKg = bahan.sumOf { it.beratKg },
            jumlahTugasLabel = 0,
            drafPenerimaanKg = 0.0,
            pesanan = emptyList()
        )
    )
}