package com.stb.sirabas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.R
import com.stb.sirabas.ui.theme.SIRABASTheme

private enum class MenuPetugasPenjualan(
    val judul: String,
    val ikon: Int
) {
    BERANDA("Beranda", R.drawable.ic_nav_beranda),
    PENJUALAN("Penjualan", R.drawable.ic_nav_penjualan),
    LAPORAN("Laporan", R.drawable.ic_nav_laporan),
    AKUN("Akun", R.drawable.ic_nav_akun)
}

@Composable
fun PenjualanMainScreen(
    beranda: @Composable () -> Unit,
    penjualan: @Composable () -> Unit,
    laporan: @Composable () -> Unit,
    akun: @Composable () -> Unit,
    usernamePanduan: String = ""
) {
    var menuAktif by rememberSaveable {
        mutableStateOf(MenuPetugasPenjualan.BERANDA.name)
    }

    val stateHalaman = rememberSaveableStateHolder()
    val warna = MaterialTheme.colorScheme

    Scaffold(
        containerColor = warna.background,
        contentColor = warna.onBackground,
        bottomBar = {
            val tabPenjualan = MenuPetugasPenjualan.values()

            val menuPanduan = tabPenjualan.map { menu ->
                MenuPanduanSirabas(
                    judul = menu.judul,
                    ikon = menu.ikon,
                    penjelasan = when (menu) {
                        MenuPetugasPenjualan.BERANDA ->
                            "Lihat tren penjualan, pilih produk, lalu tambahkan jumlah yang dipesan ke keranjang."

                        MenuPetugasPenjualan.PENJUALAN ->
                            "Cari invoice pelanggan, periksa sisa piutang, dan catat pembayaran."

                        MenuPetugasPenjualan.LAPORAN ->
                            "Lihat total penjualan, pembayaran, dan sisa piutang untuk periode yang kamu pilih."

                        MenuPetugasPenjualan.AKUN ->
                            "Edit profil, atur mode gelap, lihat informasi SIRABAS, atau keluar dari akun."
                    }
                )
            }

            NavigasiPanduanSirabas(
                username = usernamePanduan,
                peran = "penjualan",
                daftarMenu = menuPanduan,
                indeksAktif = tabPenjualan.indexOfFirst { it.name == menuAktif },
                onPilihMenu = { indeks ->
                    menuAktif = tabPenjualan[indeks].name
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
            stateHalaman.SaveableStateProvider(
                key = menuAktif
            ) {
                when (menuAktif) {
                    MenuPetugasPenjualan.BERANDA.name -> beranda()
                    MenuPetugasPenjualan.PENJUALAN.name -> penjualan()
                    MenuPetugasPenjualan.LAPORAN.name -> laporan()
                    MenuPetugasPenjualan.AKUN.name -> akun()
                }
            }
        }
    }
}

// Preview ini khusus memeriksa warna dan navigasi bawah.
// Isi halaman aplikasi tetap dikirim melalui SirabasApp.
@Composable
private fun ContohNavigasiPenjualan() {
    PenjualanMainScreen(
        beranda = {
            IsiPreviewNavigasi("Beranda Penjualan")
        },
        penjualan = {
            IsiPreviewNavigasi("Penjualan")
        },
        laporan = {
            IsiPreviewNavigasi("Laporan Penjualan")
        },
        akun = {
            IsiPreviewNavigasi("Akun")
        }
    )
}

@Composable
private fun IsiPreviewNavigasi(judul: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = judul,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(
    name = "Navigasi Penjualan · Terang",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PenjualanTerangPreview() {
    SIRABASTheme(darkTheme = false) {
        ContohNavigasiPenjualan()
    }
}

@Preview(
    name = "Navigasi Penjualan · Gelap",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PenjualanGelapPreview() {
    SIRABASTheme(darkTheme = true) {
        ContohNavigasiPenjualan()
    }
}