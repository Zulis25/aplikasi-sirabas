package com.stb.sirabas.ui

import android.content.pm.ApplicationInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.stb.sirabas.R
import com.stb.sirabas.ui.screens.*
import com.stb.sirabas.ui.screens.akun.AkunScreen
import com.stb.sirabas.ui.screens.akun.EditProfilScreen
import com.stb.sirabas.ui.screens.akun.ProfilAkunUi
import com.stb.sirabas.ui.screens.akun.TentangAplikasiScreen
import com.stb.sirabas.ui.screens.auth.LoginScreen
import com.stb.sirabas.ui.screens.auth.OnboardingScreen
import com.stb.sirabas.ui.screens.auth.PendingActivationScreen
import com.stb.sirabas.ui.screens.auth.RegisterScreen
import com.stb.sirabas.ui.screens.auth.SplashScreen
import com.stb.sirabas.ui.theme.SIRABASTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.stb.sirabas.ui.screens.LaporanPenjualanDenganExcel

private const val PERUSAHAAN = "PT. Sri Tani Berkah"

@Composable
fun SirabasApp() {
    val context = LocalContext.current
    val buildDebug =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    val temaSistemGelap = isSystemInDarkTheme()

    val preferensiTema = remember(context) {
        context.getSharedPreferences(
            "pengaturan_tampilan_sirabas",
            android.content.Context.MODE_PRIVATE
        )
    }

    var pilihanModeGelap by remember(preferensiTema) {
        mutableStateOf<Boolean?>(
            if (preferensiTema.contains("mode_gelap")) {
                preferensiTema.getBoolean("mode_gelap", false)
            } else {
                null
            }
        )
    }

    val modeGelap = pilihanModeGelap ?: temaSistemGelap

    val konteksSesi = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val sesiLogin = remember(konteksSesi) {
        konteksSesi.getSharedPreferences(
            "sirabas_sesi_login",
            android.content.Context.MODE_PRIVATE
        )
    }

    var layar by rememberSaveable {
        val usernameTersimpan = sesiLogin.getString("username", "")
        val peranTersimpan = sesiLogin.getString("peran", "")

        val layarAwal = when {
            buildDebug && !usernameTersimpan.isNullOrBlank() &&
                    peranTersimpan == "produksi" -> "produksi"

            buildDebug && !usernameTersimpan.isNullOrBlank() &&
                    peranTersimpan == "penjualan" -> "penjualan"

            sesiLogin.getBoolean("pernah_masuk", false) -> "login"

            else -> "onboarding"
        }

        mutableStateOf(layarAwal)
    }

    fun masukKeDashboard(username: String, peran: String) {
        sesiLogin.edit()
            .putString("username", username.trim())
            .putString("peran", peran)
            .putBoolean("pernah_masuk", true)
            .apply()

        layar = peran
    }
    var tampilkanSplash by rememberSaveable { mutableStateOf(true) }
    var pesan by rememberSaveable { mutableStateOf<String?>(null) }

    var namaPengajuan by rememberSaveable { mutableStateOf("") }
    var usernamePengajuan by rememberSaveable { mutableStateOf("") }
    var whatsappPengajuan by rememberSaveable { mutableStateOf("") }

    // Data frontend untuk sesi aplikasi ini.
    val daftarInvoice = remember { mutableStateListOf<InvoicePenjualanUi>() }
    val profilPerRole = remember { mutableStateMapOf<String, ProfilAkunUi>() }

    val kembaliKeLogin: () -> Unit = {
        pesan = null
        layar = "login"
    }

    LaunchedEffect(tampilkanSplash) {
        if (tampilkanSplash) {
            delay(3000)
            tampilkanSplash = false
        }
    }

    BackHandler(
        enabled = !tampilkanSplash &&
                (layar == "register" || layar == "menunggu_aktivasi")
    ) {
        kembaliKeLogin()
    }

    SIRABASTheme(darkTheme = modeGelap, dynamicColor = false) {
        if (tampilkanSplash) {
            SplashScreen()
        } else {
            when (layar) {
                "onboarding" -> OnboardingScreen(
                    onSelesai = { layar = "login" }
                )

                "login" -> LoginScreen(
                    onDaftar = { layar = "register" },
                    onMasuk = { username, sandi ->
                        val nama = username.trim()

                        when {
                            buildDebug && nama == "Dedi" &&
                                    sandi == "Produksi123" -> layar = "produksi"

                            buildDebug && nama == "Rina" &&
                                    sandi == "Penjualan123" -> layar = "penjualan"

                            usernamePengajuan.isNotBlank() &&
                                    nama == usernamePengajuan -> {
                                layar = "menunggu_aktivasi"
                            }

                            !buildDebug -> {
                                pesan = "Layanan masuk belum tersedia."
                            }

                            else -> {
                                pesan = "Username atau kata sandi tidak sesuai."
                            }
                        }
                    }
                )

                "register" -> RegisterScreen(
                    onMasuk = kembaliKeLogin,
                    onPendaftaranDiajukan = { nama, username, whatsapp ->
                        namaPengajuan = nama
                        usernamePengajuan = username.trim()
                        whatsappPengajuan = whatsapp
                        layar = "menunggu_aktivasi"
                    }
                )

                "menunggu_aktivasi" -> PendingActivationScreen(
                    nama = namaPengajuan,
                    username = usernamePengajuan,
                    whatsapp = whatsappPengajuan,
                    onMasuk = kembaliKeLogin
                )

                "produksi", "penjualan" -> {
                    val role = layar
                    val produksi = role == "produksi"
                    val namaAwal = if (produksi) "Dedi" else "Rina"

                    val profil = profilPerRole[role] ?: ProfilAkunUi(
                        namaLengkap = namaAwal,
                        username = namaAwal,
                        nomorWhatsApp = "",
                        role = if (produksi) "Petugas Produksi" else "Petugas Penjualan",
                        namaPerusahaan = PERUSAHAAN,
                        alamatPerusahaan = "",
                        teleponPerusahaan = ""
                    )

                    val halamanAkun: @Composable () -> Unit = {
                        MenuAkunSirabas(
                            profil = profil,
                            modeGelap = modeGelap,
                            onModeGelap = { gelap ->
                                pilihanModeGelap = gelap
                                preferensiTema.edit()
                                    .putBoolean("mode_gelap", gelap)
                                    .apply()
                            },
                            onSimpanProfil = { profilPerRole[role] = it },
                            onKeluar = {
                                sesiLogin.edit()
                                    .remove("username")
                                    .remove("peran")
                                    .apply()

                                kembaliKeLogin()
                            }
                        )
                    }

                    if (produksi) {
                        val bahan = remember { contohBahanBaku() }
                        val tanggal = remember {
                            SimpleDateFormat(
                                "dd MMMM yyyy",
                                Locale.forLanguageTag("id-ID")
                            ).format(Date())
                        }

                        ProductionMainScreen(
                            usernamePanduan = profil.username,
                            dataBeranda = ProductionHomeUi(
                                namaPetugas = profil.namaLengkap,
                                tanggal = tanggal,
                                penerimaanHariIniKg = 0.0,
                                jumlahPenerimaan = 0,
                                stokBahanBakuKg = bahan.sumOf { it.beratKg },
                                jumlahTugasLabel = 0,
                                drafPenerimaanKg = 0.0,
                                pesanan = daftarInvoice.flatMap { invoice ->
                                    invoice.daftarItem.mapNotNull { item ->
                                        val produk = item.produk

                                        if (!produk.satuanJual.equals("karung", ignoreCase = true)) {
                                            return@mapNotNull null
                                        }

                                        val beratKg = produk.kemasan
                                            .trim()
                                            .substringBefore(" ")
                                            .toIntOrNull()
                                            ?: return@mapNotNull null

                                        ProductionOrderUi(
                                            nomorPesanan = invoice.nomor,
                                            namaPelanggan = invoice.pelanggan,
                                            namaProduk = produk.nama,
                                            beratPerKarungKg = beratKg,
                                            jumlahKarung = item.jumlah,
                                            kemasan = produk.kemasan,
                                            tanggalPesanan = invoice.tanggal
                                        )
                                    }
                                }
                            ),
                            dataBahanBaku = bahan,
                            halamanAkun = halamanAkun
                        )
                    } else {
                        DashboardPenjualanSirabas(
                            profil = profil,
                            daftarInvoice = daftarInvoice,
                            onInvoiceBaru = { invoice ->
                                daftarInvoice.add(0, invoice)
                            },
                            onInvoiceDiperbarui = { invoiceBaru ->
                                val indeks = daftarInvoice.indexOfFirst { it.nomor == invoiceBaru.nomor }
                                if (indeks >= 0) {
                                    daftarInvoice[indeks] = invoiceBaru
                                }
                            },
                            halamanAkun = halamanAkun,
                            onPesan = { pesan = it }
                        )
                    }
                }
            }
        }

        pesan?.let { isi ->
            AlertDialog(
                onDismissRequest = { pesan = null },
                title = { Text("Pemberitahuan") },
                text = { Text(isi) },
                confirmButton = {
                    TextButton(onClick = { pesan = null }) {
                        Text("Tutup")
                    }
                }
            )
        }
    }
}

@Composable
private fun MenuAkunSirabas(
    profil: ProfilAkunUi,
    modeGelap: Boolean,
    onModeGelap: (Boolean) -> Unit,
    onSimpanProfil: (ProfilAkunUi) -> Unit,
    onKeluar: () -> Unit
) {
    var halaman by rememberSaveable { mutableStateOf("akun") }
    var bukaBahasa by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = halaman != "akun") { halaman = "akun" }

    when (halaman) {
        "tentang" -> TentangAplikasiScreen(
            onKembali = { halaman = "akun" }
        )

        "edit" -> EditProfilScreen(
            profil = profil,
            onKembali = { halaman = "akun" },
            onSimpan = { hasil ->
                onSimpanProfil(hasil)
                halaman = "akun"
            }
        )

        else -> AkunScreen(
            profil = profil,
            bahasa = "Bahasa Indonesia",
            modeGelap = modeGelap,
            onEditProfil = { halaman = "edit" },
            onPilihBahasa = { bukaBahasa = true },
            onUbahModeGelap = onModeGelap,
            onTentang = { halaman = "tentang" },
            onKeluar = onKeluar
        )
    }

    if (bukaBahasa) {
        AlertDialog(
            onDismissRequest = { bukaBahasa = false },
            title = { Text("Bahasa aplikasi") },
            text = { Text("Bahasa Indonesia") },
            confirmButton = {
                TextButton(onClick = { bukaBahasa = false }) {
                    Text("Selesai")
                }
            }
        )
    }
}

@Composable
private fun DashboardPenjualanSirabas(
    profil: ProfilAkunUi,
    daftarInvoice: List<InvoicePenjualanUi>,
    onInvoiceBaru: (InvoicePenjualanUi) -> Unit,
    onInvoiceDiperbarui: (InvoicePenjualanUi) -> Unit,
    halamanAkun: @Composable () -> Unit,
    onPesan: (String) -> Unit
) {
    val utama = remember {
        listOf(
            ProdukPenjualanUi(
                "PREMIUM-25-HIJAU", "Beras Premium", "25 kg hijau",
                "karung", 12, 350_000L,
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

    val tren = remember {
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

    PenjualanMainScreen(
        usernamePanduan = profil.username,
        beranda = {
            AlurPenjualanScreen(
                namaPetugas = profil.namaLengkap,
                namaPt = profil.namaPerusahaan,
                alamatPt = profil.alamatPerusahaan,
                teleponPt = profil.teleponPerusahaan,
                trenHarian = tren,
                hasilProduksi = utama,
                hasilSamping = samping,
                daftarPelanggan = listOf("MBG A", "MBG B"),
                onKonfirmasiInvoice = onInvoiceBaru
            )
        },
        penjualan = {
            RiwayatPenjualanScreen(
                daftarInvoice = daftarInvoice,
                namaPt = profil.namaPerusahaan,
                alamatPt = profil.alamatPerusahaan,
                teleponPt = profil.teleponPerusahaan,
                onInvoiceDiperbarui = onInvoiceDiperbarui
            )
        },
        laporan = {
            LaporanPenjualanDenganExcel(
                daftarInvoice = daftarInvoice,
                onPesan = onPesan
            )
        },
        akun = halamanAkun
    )
}