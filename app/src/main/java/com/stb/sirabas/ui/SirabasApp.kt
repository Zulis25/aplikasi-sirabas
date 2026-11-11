package com.stb.sirabas.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.stb.sirabas.R
import com.stb.sirabas.ui.screens.AlurPenjualanScreen
import com.stb.sirabas.ui.screens.auth.LoginScreen
import com.stb.sirabas.ui.screens.auth.OnboardingScreen
import com.stb.sirabas.ui.screens.auth.PendingActivationScreen
import com.stb.sirabas.ui.screens.PenjualanHarianUi
import com.stb.sirabas.ui.screens.ProdukPenjualanUi
import com.stb.sirabas.ui.screens.auth.RegisterScreen
import com.stb.sirabas.ui.screens.auth.SplashScreen
import com.stb.sirabas.ui.theme.SIRABASTheme
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun SirabasApp() {
    var layar by rememberSaveable { mutableStateOf("onboarding") }
    var tampilkanSplash by rememberSaveable { mutableStateOf(true) }

    var namaPengajuan by rememberSaveable { mutableStateOf("") }
    var usernamePengajuan by rememberSaveable { mutableStateOf("") }
    var whatsappPengajuan by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (tampilkanSplash) {
            delay(3000)
            tampilkanSplash = false
        }
    }

    val kembaliKeLogin: () -> Unit = {
        namaPengajuan = ""
        usernamePengajuan = ""
        whatsappPengajuan = ""
        layar = "login"
    }

    BackHandler(
        enabled = !tampilkanSplash &&
                (layar == "register" || layar == "menunggu_aktivasi")
    ) {
        kembaliKeLogin()
    }

    if (tampilkanSplash) {
        SplashScreen()
        return
    }

    when (layar) {
        "onboarding" -> {
            OnboardingScreen(
                onSelesai = { layar = "login" }
            )
        }

        "login" -> {
            LoginScreen(
                onDaftar = { layar = "register" }
            )
        }

        "register" -> {
            RegisterScreen(
                onMasuk = kembaliKeLogin,
                onPendaftaranDiajukan = { nama, username, whatsapp ->
                    namaPengajuan = nama
                    usernamePengajuan = username
                    whatsappPengajuan = whatsapp
                    layar = "menunggu_aktivasi"
                }
            )
        }

        "menunggu_aktivasi" -> {
            PendingActivationScreen(
                nama = namaPengajuan,
                username = usernamePengajuan,
                whatsapp = whatsappPengajuan,
                onMasuk = kembaliKeLogin
            )
        }
    }
}

// Jalankan Preview ini untuk mencoba Tambah → Checkout di HP.
@Preview(
    name = "Petugas Penjualan · Beranda dan Checkout",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun AlurPenjualanDiHpPreview() {
    val hasilProduksi = remember {
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

    val hasilSamping = remember {
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

    val trenHarian = remember {
        listOf(
            PenjualanHarianUi("5 Nov", 700_000L),
            PenjualanHarianUi("6 Nov", 1_050_000L),
            PenjualanHarianUi("7 Nov", 850_000L),
            PenjualanHarianUi("8 Nov", 1_400_000L),
            PenjualanHarianUi("9 Nov", 1_100_000L),
            PenjualanHarianUi("10 Nov", 1_750_000L),
            PenjualanHarianUi("11 Nov", 1_400_000L)
        )
    }

    SIRABASTheme {
        AlurPenjualanScreen(
            namaPetugas = "Rina",
            namaPt = "PT Sri Tani Berkah",
            alamatPt = "",
            teleponPt = "",
            trenHarian = trenHarian,
            hasilProduksi = hasilProduksi,
            hasilSamping = hasilSamping,
            daftarPelanggan = listOf("MBG A", "MBG B"),
            onKonfirmasiInvoice = { _, _, _, _, _ ->
                // Penyimpanan invoice akan dihubungkan ke data penjualan.
            }
        )
    }
}