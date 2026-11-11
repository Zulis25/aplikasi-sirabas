package com.stb.sirabas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.R
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.util.Calendar
import java.util.Locale

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
    akun: @Composable () -> Unit
) {
    var menuAktif by rememberSaveable {
        mutableStateOf(MenuPetugasPenjualan.BERANDA.name)
    }

    val stateHalaman = rememberSaveableStateHolder()

    Scaffold(
        containerColor = AccessColors.Background,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                MenuPetugasPenjualan.values().forEach { menu ->
                    NavigationBarItem(
                        selected = menuAktif == menu.name,
                        onClick = { menuAktif = menu.name },
                        icon = {
                            Icon(
                                painter = painterResource(menu.ikon),
                                contentDescription = null,
                                modifier = Modifier.size(23.dp)
                            )
                        },
                        label = {
                            Text(menu.judul, maxLines = 1)
                        },
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
            stateHalaman.SaveableStateProvider(menuAktif) {
                when (MenuPetugasPenjualan.valueOf(menuAktif)) {
                    MenuPetugasPenjualan.BERANDA -> beranda()
                    MenuPetugasPenjualan.PENJUALAN -> penjualan()
                    MenuPetugasPenjualan.LAPORAN -> laporan()
                    MenuPetugasPenjualan.AKUN -> akun()
                }
            }
        }
    }
}

@Preview(
    name = "Navigasi Petugas Penjualan",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PenjualanMainPreview() {
    val utama = remember {
        listOf(
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
                "PREMIUM-5", "Beras Premium", "5 kg",
                "karung", 20, 75_000L
            ),
            ProdukPenjualanUi(
                "PREMIUM-10", "Beras Premium", "10 kg",
                "karung", 15, 145_000L
            ),
            ProdukPenjualanUi(
                "PREMIUM-25-BIRU", "Beras Premium", "25 kg biru muda",
                "karung", 8, 350_000L
            ),
            ProdukPenjualanUi(
                "MEDIUM-25", "Beras Medium", "25 kg",
                "karung", 10, 310_000L
            ),
            ProdukPenjualanUi(
                "BERAS-50", "Beras", "50 kg",
                "karung", 5, 600_000L
            )
        )
    }

    val samping = remember {
        listOf(
            "Dedak Halus", "Dedak Kasar", "Menir",
            "Reject", "Broken", "Sekam"
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
    }

    val invoice = remember {
        mutableStateListOf(
            InvoicePenjualanUi(
                nomor = "INV/2026/002",
                tanggal = "11 November 2026",
                pelanggan = "MBG B",
                daftarItem = listOf(ItemPesananUi(utama.first(), 2, 350_000L)),
                jumlahDibayar = 200_000L,
                metodePembayaran = "Cash"
            ),
            InvoicePenjualanUi(
                nomor = "INV/2026/001",
                tanggal = "10 November 2026",
                pelanggan = "MBG A",
                daftarItem = listOf(ItemPesananUi(utama.first(), 3, 350_000L)),
                jumlahDibayar = 1_050_000L,
                metodePembayaran = "Transfer Bank"
            )
        )
    }

    val tren = remember {
        listOf(
            PenjualanHarianUi("10 Nov", 1_050_000L),
            PenjualanHarianUi("11 Nov", 700_000L)
        )
    }

    val tanggalContoh = remember {
        Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.NOVEMBER, 11)
        }.time
    }

    SIRABASTheme {
        PenjualanMainScreen(
            beranda = {
                AlurPenjualanScreen(
                    namaPetugas = "Rina",
                    namaPt = "PT Sri Tani Berkah",
                    alamatPt = "",
                    teleponPt = "",
                    trenHarian = tren,
                    hasilProduksi = utama,
                    hasilSamping = samping,
                    daftarPelanggan = listOf("MBG A", "MBG B"),
                    onKonfirmasiInvoice = { invoiceBaru ->
                        invoice.add(0, invoiceBaru)
                    }
                )
            },
            penjualan = {
                RiwayatPenjualanScreen(
                    daftarInvoice = invoice,
                    namaPt = "PT Sri Tani Berkah",
                    alamatPt = "",
                    teleponPt = "",
                    onUnduhGambar = {},
                    onCetak = {}
                )
            },
            laporan = {
                LaporanPenjualanScreen(
                    daftarInvoice = invoice,
                    tanggalAcuan = tanggalContoh,
                    onUnduhExcel = { _, _, _ -> }
                )
            },
            akun = {
                // Isi khusus Preview. Slot ini bisa memakai AkunScreen milikmu.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AccessColors.Background)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Akun",
                        color = AccessColors.Ink,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Rina",
                                color = AccessColors.Ink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text("Petugas Penjualan", color = AccessColors.Green)
                            Text("PT Sri Tani Berkah", color = AccessColors.Muted)
                        }
                    }
                }
            }
        )
    }
}