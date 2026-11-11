package com.stb.sirabas.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stb.sirabas.R
import com.stb.sirabas.ui.theme.SIRABASTheme

private enum class TabAdmin(val judul: String) {
    BERANDA("Beranda"),
    AKTIVASI("Aktivasi"),
    PENGGUNA("Pengguna"),
    LABEL("Label")
}

@Composable
fun AdminMainScreen(
    ringkasan: RingkasanAdminUi,
    daftarPengajuan: List<PengajuanAkunUi>,
    daftarPengguna: List<PenggunaAktifAdminUi>,
    daftarKarung: List<JejakKarungUi> = emptyList(),
    onAktifkanAkun: (PengajuanAkunUi, String) -> Unit
) {
    var tabAktif by rememberSaveable {
        mutableStateOf(TabAdmin.BERANDA.name)
    }

    val stateHalaman = rememberSaveableStateHolder()

    Scaffold(
        containerColor = AccessColors.Background,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                TabAdmin.values().forEach { tab ->
                    NavigationBarItem(
                        selected = tabAktif == tab.name,
                        onClick = { tabAktif = tab.name },
                        icon = { IkonMenuAdmin(tab) },
                        label = { Text(tab.judul, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccessColors.Green,
                            selectedTextColor = AccessColors.Green,
                            indicatorColor = Color(0xFFE2F2E8),
                            unselectedIconColor = AccessColors.Muted,
                            unselectedTextColor = AccessColors.Muted
                        )
                    )
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
            stateHalaman.SaveableStateProvider(tabAktif) {
                when (TabAdmin.valueOf(tabAktif)) {
                    TabAdmin.BERANDA -> {
                        BerandaAdminScreen(
                            ringkasan = ringkasan,
                            jumlahPengajuan = daftarPengajuan.size,
                            onBukaPengajuan = {
                                tabAktif = TabAdmin.AKTIVASI.name
                            }
                        )
                    }

                    TabAdmin.AKTIVASI -> {
                        AktivasiAkunScreen(
                            daftarPengajuan = daftarPengajuan,
                            onAktifkan = onAktifkanAkun
                        )
                    }

                    TabAdmin.PENGGUNA -> {
                        DaftarPenggunaAdminScreen(
                            daftarPengguna = daftarPengguna
                        )
                    }

                    TabAdmin.LABEL -> {
                        CariLabelKarungScreen(daftarKarung = daftarKarung)
                    }
                }
            }
        }
    }
}

@Composable
private fun IkonMenuAdmin(tab: TabAdmin) {
    if (tab == TabAdmin.LABEL) {
        // Ikon barcode khusus navbar. Warnanya mengikuti tab aktif.
        val warna = LocalContentColor.current

        Canvas(modifier = Modifier.size(23.dp)) {
            val unit = size.minDimension / 24f
            val posisiGaris = listOf(3f, 6f, 10f, 13f, 17f, 21f)
            val lebarGaris = listOf(1f, 2f, 1f, 2f, 2f, 1f)

            posisiGaris.forEachIndexed { indeks, posisi ->
                drawLine(
                    color = warna,
                    start = Offset(posisi * unit, 4f * unit),
                    end = Offset(posisi * unit, 20f * unit),
                    strokeWidth = lebarGaris[indeks] * unit
                )
            }
        }
    } else {
        val resource = when (tab) {
            TabAdmin.BERANDA -> R.drawable.ic_nav_beranda
            TabAdmin.AKTIVASI -> R.drawable.sirabas_clock
            TabAdmin.PENGGUNA -> R.drawable.ic_nav_akun
            TabAdmin.LABEL -> return
        }

        Icon(
            painter = painterResource(resource),
            contentDescription = null,
            modifier = Modifier.size(23.dp)
        )
    }
}

@Preview(
    name = "Admin · Navigasi lengkap",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun AdminMainPreview() {
    val pengajuan = remember {
        mutableStateListOf(
            PengajuanAkunUi(
                id = "PG-001",
                nama = "Rina Putri",
                username = "RinaPutri",
                whatsapp = "0812 0000 0001",
                tanggalPengajuan = "11 November 2026"
            ),
            PengajuanAkunUi(
                id = "PG-002",
                nama = "Dedi Saputra",
                username = "DediSaputra",
                whatsapp = "0812 0000 0002",
                tanggalPengajuan = "10 November 2026"
            )
        )
    }

    val pengguna = remember {
        mutableStateListOf(
            PenggunaAktifAdminUi(
                id = "USR-ADMIN",
                nama = "Admin STB",
                username = "AdminSTB",
                role = "Admin"
            )
        )
    }

    val ringkasan = RingkasanAdminUi(
        namaAdmin = "Admin STB",
        tanggal = "11 November 2026",
        bahanMasuk = "1.000 kg",
        hasilUtama = "180 kg",
        hasilSamping = "10 kg",
        stokBahanBaku = "1.500 kg",
        totalPenjualan = "Rp700.000",
        jumlahInvoice = 1,
        sisaPiutang = "Rp500.000"
    )

    val karungDiGudang = JejakKarungUi(
        kodeKarung = "PR001-K001",
        namaProduk = "Beras Premium",
        kemasan = "5 kg",
        nomorKarung = 1,
        jumlahKarung = 36,
        kodeLot = "BM/025",
        jenisBahan = "Beras",
        supplier = "UD Zulis",
        asalDaerah = "Maros",
        tanggalMasuk = "11 Nov 2026",
        kodeBatch = "PR/001",
        tanggalProduksi = "11 Nov 2026"
    )

    // Karung kedua berasal dari batch yang sama, tetapi sudah dikirim.
    val karungDikirim = karungDiGudang.copy(
        kodeKarung = "PR001-K002",
        nomorKarung = 2,
        pengiriman = PengirimanLabelUi(
            nomorPesanan = "PS-001",
            pelanggan = "MBG A",
            waktuPindai = "11 Nov 2026, 14.10",
            waktuKeluar = "11 Nov 2026, 14.25"
        )
    )

    SIRABASTheme {
        AdminMainScreen(
            ringkasan = ringkasan,
            daftarPengajuan = pengajuan,
            daftarPengguna = pengguna,
            daftarKarung = listOf(karungDiGudang, karungDikirim),
            onAktifkanAkun = { akun, role ->
                if (pengajuan.remove(akun)) {
                    pengguna.add(
                        PenggunaAktifAdminUi(
                            id = akun.id,
                            nama = akun.nama,
                            username = akun.username,
                            role = role
                        )
                    )
                }
            }
        )
    }
}