package com.stb.sirabas.ui

import android.content.pm.ApplicationInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
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

import com.stb.sirabas.data.remote.ApiClient
import com.stb.sirabas.data.remote.TokenManager
import com.stb.sirabas.data.repository.AuthRepository
import com.stb.sirabas.data.repository.AuthResult
import com.stb.sirabas.data.repository.ApiResult
import com.stb.sirabas.data.repository.PenjualanRepository
import com.stb.sirabas.data.repository.ProduksiRepository
import kotlinx.coroutines.launch

private const val PERUSAHAAN = "PT. Sri Tani Berkah"

@Composable
fun SirabasApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val buildDebug =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    // Network & Repository Instances
    val tokenManager = remember(context) { TokenManager(context) }
    val api = remember(tokenManager) { ApiClient.getApi(tokenManager) }
    val authRepository = remember(api, tokenManager) { AuthRepository(api, tokenManager) }
    val produksiRepository = remember(api) { ProduksiRepository(api) }
    val penjualanRepository = remember(api) { PenjualanRepository(api) }

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
    var statusAktivasiAkun by rememberSaveable { mutableStateOf("MENUNGGU_AKTIVASI") }
    var lastPassword by rememberSaveable { mutableStateOf("") }

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
                            buildDebug && nama == "Dedi" && sandi == "Produksi123" -> {
                                masukKeDashboard(nama, "produksi")
                            }

                            buildDebug && nama == "Rina" && sandi == "Penjualan123" -> {
                                masukKeDashboard(nama, "penjualan")
                            }

                            else -> {
                                scope.launch {
                                    when (val res = authRepository.login(nama, sandi)) {
                                        is AuthResult.Success -> {
                                            val usernameSaved = res.data.username
                                            val namaLengkapSaved = res.data.nama_lengkap ?: usernameSaved
                                            val roleApi = if (res.data.role.equals("produksi", true)) "produksi" else "penjualan"

                                            sesiLogin.edit()
                                                .putString("username", usernameSaved)
                                                .putString("nama_lengkap", namaLengkapSaved)
                                                .putString("peran", roleApi)
                                                .putBoolean("pernah_masuk", true)
                                                .apply()

                                            masukKeDashboard(usernameSaved, roleApi)
                                        }

                                        is AuthResult.PendingActivation -> {
                                            usernamePengajuan = nama
                                            lastPassword = sandi
                                            statusAktivasiAkun = "MENUNGGU_AKTIVASI"
                                            layar = "menunggu_aktivasi"
                                        }

                                        is AuthResult.Error -> {
                                            if (res.statusCode == 403) {
                                                usernamePengajuan = nama
                                                lastPassword = sandi
                                                statusAktivasiAkun = "NON_AKTIF"
                                                layar = "menunggu_aktivasi"
                                            } else {
                                                pesan = res.message
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                "register" -> RegisterScreen(
                    onMasuk = kembaliKeLogin,
                    onPendaftaranDiajukan = { nama, username, whatsapp, password ->
                        scope.launch {
                            when (val res = authRepository.register(username, password, nama, whatsapp, PERUSAHAAN)) {
                                is AuthResult.Success -> {
                                    namaPengajuan = nama
                                    usernamePengajuan = username.trim()
                                    whatsappPengajuan = whatsapp
                                    lastPassword = password
                                    statusAktivasiAkun = "MENUNGGU_AKTIVASI"
                                    layar = "menunggu_aktivasi"
                                }

                                is AuthResult.Error -> {
                                    pesan = res.message
                                }

                                else -> {}
                            }
                        }
                    }
                )

                "menunggu_aktivasi" -> PendingActivationScreen(
                    nama = namaPengajuan,
                    username = usernamePengajuan,
                    whatsapp = whatsappPengajuan,
                    statusAktivasi = statusAktivasiAkun,
                    onCekStatus = {
                        scope.launch {
                            if (usernamePengajuan.isNotBlank() && lastPassword.isNotBlank()) {
                                when (val res = authRepository.login(usernamePengajuan, lastPassword)) {
                                    is AuthResult.Success -> {
                                        statusAktivasiAkun = "AKTIF"
                                        val roleApi = if (res.data.role.equals("produksi", true)) "produksi" else "penjualan"
                                        masukKeDashboard(res.data.username, roleApi)
                                    }

                                    is AuthResult.PendingActivation -> {
                                        statusAktivasiAkun = "MENUNGGU_AKTIVASI"
                                        pesan = "Akun Anda masih menunggu aktivasi oleh Admin."
                                    }

                                    is AuthResult.Error -> {
                                        statusAktivasiAkun = "NON_AKTIF"
                                        pesan = "Status akun: Non-aktif atau ditolak (${res.message})."
                                    }
                                }
                            } else {
                                pesan = "Silakan login kembali melalui halaman utama."
                            }
                        }
                    },
                    onMasuk = kembaliKeLogin
                )

                "produksi", "penjualan" -> {
                    val role = layar
                    val produksi = role == "produksi"
                    val namaAwal = if (produksi) "Dedi" else "Rina"

                    val usernameTersimpan = sesiLogin.getString("username", namaAwal) ?: namaAwal
                    val namaLengkapTersimpan = sesiLogin.getString("nama_lengkap", namaAwal) ?: namaAwal

                    val profil = profilPerRole[role] ?: ProfilAkunUi(
                        namaLengkap = namaLengkapTersimpan,
                        username = usernameTersimpan,
                        nomorWhatsApp = "",
                        role = if (produksi) "Petugas Produksi" else "Petugas Penjualan",
                        namaPerusahaan = PERUSAHAAN,
                        alamatPerusahaan = "",
                        teleponPerusahaan = ""
                    )

                    val halamanAkun: @Composable () -> Unit = {
                        MenuAkunSirabas(
                            profilAwal = profil,
                            authRepository = authRepository,
                            modeGelap = modeGelap,
                            onModeGelap = { gelap ->
                                pilihanModeGelap = gelap
                                preferensiTema.edit()
                                    .putBoolean("mode_gelap", gelap)
                                    .apply()
                            },
                            onKeluar = {
                                authRepository.logout()
                                sesiLogin.edit()
                                    .remove("username")
                                    .remove("nama_lengkap")
                                    .remove("peran")
                                    .putBoolean("pernah_masuk", false)
                                    .apply()

                                kembaliKeLogin()
                            },
                            onPesan = { pesan = it }
                        )
                    }

                    if (produksi) {
                        DashboardProduksiSirabas(
                            profil = profil,
                            produksiRepository = produksiRepository,
                            penjualanRepository = penjualanRepository,
                            daftarInvoice = daftarInvoice,
                            halamanAkun = halamanAkun,
                            onPesan = { pesan = parseApiErrorMessage(it) }
                        )
                    } else {
                        DashboardPenjualanSirabas(
                            profil = profil,
                            penjualanRepository = penjualanRepository,
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
    profilAwal: ProfilAkunUi,
    authRepository: AuthRepository,
    modeGelap: Boolean,
    onModeGelap: (Boolean) -> Unit,
    onKeluar: () -> Unit,
    onPesan: (String) -> Unit
) {
    var profil by remember { mutableStateOf(profilAwal) }
    var halaman by rememberSaveable { mutableStateOf("akun") }
    var bukaBahasa by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            when (val res = authRepository.getProfile()) {
                is AuthResult.Success -> {
                    val p = res.data
                    val roleLabel = if (p.role.equals("produksi", true)) "Petugas Produksi" else "Petugas Penjualan"
                    profil = ProfilAkunUi(
                        namaLengkap = p.nama_lengkap ?: p.username,
                        username = p.username,
                        nomorWhatsApp = p.nomor_whatsapp ?: "",
                        role = roleLabel,
                        namaPerusahaan = p.nama_perusahaan ?: "PT. Sri Tani Berkah",
                        alamatPerusahaan = "Kantor Pusat",
                        teleponPerusahaan = p.nomor_whatsapp ?: ""
                    )
                }
                else -> {}
            }
        }
    }

    BackHandler(enabled = halaman != "akun") { halaman = "akun" }

    when (halaman) {
        "tentang" -> TentangAplikasiScreen(
            onKembali = { halaman = "akun" }
        )

        "edit" -> EditProfilScreen(
            profil = profil,
            onKembali = { halaman = "akun" },
            onSimpan = { hasilBaru ->
                scope.launch {
                    when (val res = authRepository.updateProfile(
                        namaLengkap = hasilBaru.namaLengkap,
                        nomorWhatsapp = hasilBaru.nomorWhatsApp,
                        namaPerusahaan = hasilBaru.namaPerusahaan
                    )) {
                        is AuthResult.Success -> {
                            val p = res.data
                            val roleLabel = if (p.role.equals("produksi", true)) "Petugas Produksi" else "Petugas Penjualan"
                            profil = ProfilAkunUi(
                                namaLengkap = p.nama_lengkap ?: p.username,
                                username = p.username,
                                nomorWhatsApp = p.nomor_whatsapp ?: "",
                                role = roleLabel,
                                namaPerusahaan = p.nama_perusahaan ?: "PT. Sri Tani Berkah",
                                alamatPerusahaan = "Kantor Pusat",
                                teleponPerusahaan = p.nomor_whatsapp ?: ""
                            )
                            halaman = "akun"
                            onPesan("Profil berhasil diperbarui di server.")
                        }
                        is AuthResult.Error -> {
                            onPesan(res.message)
                        }
                        else -> {}
                    }
                }
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
    penjualanRepository: PenjualanRepository,
    daftarInvoice: MutableList<InvoicePenjualanUi>,
    onInvoiceBaru: (InvoicePenjualanUi) -> Unit,
    onInvoiceDiperbarui: (InvoicePenjualanUi) -> Unit,
    halamanAkun: @Composable () -> Unit,
    onPesan: (String) -> Unit
) {
    var produkUtama by remember { mutableStateOf<List<ProdukPenjualanUi>>(emptyList()) }
    var produkSamping by remember { mutableStateOf<List<ProdukPenjualanUi>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val loadData = remember(penjualanRepository) {
        {
            scope.launch {
                isRefreshing = true
                when (val res = penjualanRepository.getProducts()) {
                    is ApiResult.Success -> {
                        val list = res.data.orEmpty()
                        produkUtama = list.filter { !it.jenis_produk.equals("samping", true) && !it.kemasan.equals("curah", true) }
                            .map { p ->
                                val foto = when (p.kode) {
                                    "PREMIUM-5" -> R.drawable.premium_5kg
                                    "PREMIUM-10" -> R.drawable.premium_10kg
                                    "PREMIUM-25-HIJAU" -> R.drawable.premium_25kg_hijau
                                    "PREMIUM-25-BIRU" -> R.drawable.premium_25kg_biru
                                    "MEDIUM-25" -> R.drawable.medium_25kg
                                    "BERAS-50" -> R.drawable.beras_50kg
                                    else -> null
                                }
                                ProdukPenjualanUi(
                                    kode = p.kode,
                                    nama = p.nama,
                                    kemasan = p.kemasan,
                                    satuanJual = "karung",
                                    stokTersedia = p.stok_tersedia,
                                    hargaKatalog = p.harga_katalog,
                                    fotoRes = foto
                                )
                            }

                        produkSamping = list.filter { it.jenis_produk.equals("samping", true) || it.kemasan.equals("curah", true) }
                            .map { p ->
                                ProdukPenjualanUi(
                                    kode = p.kode,
                                    nama = p.nama,
                                    kemasan = p.kemasan,
                                    satuanJual = "kg",
                                    stokTersedia = p.stok_tersedia,
                                    hargaKatalog = p.harga_katalog
                                )
                            }
                    }
                    is ApiResult.Error -> {
                        onPesan(res.message)
                    }
                }

                when (val resInv = penjualanRepository.getInvoices()) {
                    is ApiResult.Success -> {
                        val invs = resInv.data.orEmpty().map { inv ->
                            InvoicePenjualanUi(
                                nomor = inv.nomor_invoice,
                                tanggal = inv.tanggal.orEmpty(),
                                pelanggan = inv.pelanggan.orEmpty(),
                                daftarItem = inv.items.orEmpty().map { item ->
                                    ItemPesananUi(
                                        produk = ProdukPenjualanUi(
                                            kode = item.kode_produk.orEmpty(),
                                            nama = item.nama_produk.orEmpty(),
                                            kemasan = item.kemasan.orEmpty(),
                                            satuanJual = "karung",
                                            stokTersedia = 99,
                                            hargaKatalog = item.harga_satuan
                                        ),
                                        jumlah = item.jumlah,
                                        hargaSatuan = item.harga_satuan
                                    )
                                },
                                jumlahDibayar = inv.jumlah_dibayar,
                                metodePembayaran = inv.pembayaran.orEmpty().lastOrNull()?.metode_bayar,
                                riwayatPembayaran = inv.pembayaran.orEmpty().map { pay ->
                                    PembayaranInvoiceUi(
                                        waktu = pay.waktu.orEmpty(),
                                        nominal = pay.jumlah,
                                        metode = pay.metode_bayar ?: "TUNAI"
                                    )
                                }
                            )
                        }
                        daftarInvoice.clear()
                        daftarInvoice.addAll(invs)
                    }
                    is ApiResult.Error -> {}
                }
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
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

    val defaultUtama = listOf(
        ProdukPenjualanUi("PREMIUM-25-HIJAU", "Beras Premium", "25 kg hijau", "karung", 12, 350_000L, R.drawable.premium_25kg_hijau),
        ProdukPenjualanUi("PREMIUM-5", "Beras Premium", "5 kg", "karung", 20, 75_000L, R.drawable.premium_5kg),
        ProdukPenjualanUi("PREMIUM-10", "Beras Premium", "10 kg", "karung", 15, 145_000L, R.drawable.premium_10kg),
        ProdukPenjualanUi("MEDIUM-25", "Beras Medium", "25 kg", "karung", 10, 310_000L, R.drawable.medium_25kg),
        ProdukPenjualanUi("BERAS-50", "Beras", "50 kg", "karung", 5, 600_000L, R.drawable.beras_50kg)
    )

    val defaultSamping = listOf(
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

    PenjualanMainScreen(
        usernamePanduan = profil.username,
        beranda = {
            AlurPenjualanScreen(
                namaPetugas = profil.namaLengkap,
                namaPt = profil.namaPerusahaan,
                alamatPt = profil.alamatPerusahaan,
                teleponPt = profil.teleponPerusahaan,
                trenHarian = tren,
                hasilProduksi = produkUtama,
                hasilSamping = produkSamping,
                daftarPelanggan = listOf("MBG A", "MBG B"),
                onKonfirmasiInvoice = { newInv ->
                    scope.launch {
                        val req = com.stb.sirabas.data.remote.InvoiceRequest(
                            nomor_invoice = newInv.nomor,
                            pelanggan = newInv.pelanggan,
                            items = newInv.daftarItem.map { itm ->
                                com.stb.sirabas.data.remote.InvoiceItemRequest(
                                    kode_produk = itm.produk.kode,
                                    jumlah = itm.jumlah
                                )
                            }
                        )
                        when (val res = penjualanRepository.createInvoice(req)) {
                            is ApiResult.Success -> {
                                onInvoiceBaru(newInv)
                                onPesan("Invoice ${newInv.nomor} berhasil dibuat di server.")
                                loadData()
                            }
                            is ApiResult.Error -> {
                                onPesan(res.message)
                            }
                        }
                    }
                }
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

@Composable
private fun DashboardProduksiSirabas(
    profil: ProfilAkunUi,
    produksiRepository: ProduksiRepository,
    penjualanRepository: PenjualanRepository,
    daftarInvoice: List<InvoicePenjualanUi>,
    halamanAkun: @Composable () -> Unit,
    onPesan: (String) -> Unit
) {
    var daftarBahan by remember { mutableStateOf<List<BahanBakuUi>>(emptyList()) }
    var daftarBatch by remember { mutableStateOf<List<BatchProduksiUi>>(emptyList()) }
    var katalogProduk by remember { mutableStateOf<List<com.stb.sirabas.data.remote.ProductDto>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val loadData: () -> Unit = {
        scope.launch {
            isRefreshing = true
            when (val resProd = penjualanRepository.getProducts()) {
                is ApiResult.Success -> {
                    katalogProduk = resProd.data.orEmpty()
                }
                is ApiResult.Error -> {}
            }

            when (val res = produksiRepository.getRawMaterials()) {
                is ApiResult.Success -> {
                    daftarBahan = res.data.orEmpty().map { raw ->
                        BahanBakuUi(
                            kode = raw.nomor_penerimaan,
                            tanggal = raw.tanggal,
                            jenis = raw.jenis,
                            supplier = raw.supplier,
                            asalDaerah = raw.asal_daerah,
                            beratKg = raw.berat_masuk_kg,
                            kadarAir = raw.kadar_air,
                            derajatSosoh = raw.derajat_sosoh,
                            broken = raw.broken,
                            petugas = profil.username
                        )
                    }
                }
                is ApiResult.Error -> {
                    onPesan(parseApiErrorMessage(res.message))
                }
            }

            when (val resBatch = produksiRepository.getProductionBatches()) {
                is ApiResult.Success -> {
                    daftarBatch = resBatch.data.orEmpty().map { b ->
                        BatchProduksiUi(
                            kode = b.kode_batch,
                            tanggal = b.tanggal ?: "",
                            kodeBahan = b.nomor_penerimaan ?: "",
                            beratInput = b.berat_input_kg?.toBigDecimal() ?: java.math.BigDecimal.ZERO,
                            rincian = RincianHasilProduksiUi(
                                hasilUtama = b.hasil_utama.orEmpty().map { hu ->
                                    HasilUtamaProduksiUi(
                                        namaProduk = hu.kode_produk ?: "Produk",
                                        kemasan = "karung",
                                        beratKg = hu.berat_kg?.toBigDecimal() ?: java.math.BigDecimal.ZERO
                                    )
                                },
                                hasilSamping = b.hasil_samping.orEmpty().map { hs ->
                                    HasilSampingProduksiUi(
                                        namaProduk = hs.nama_produk ?: "Samping",
                                        beratKg = hs.berat_kg?.toBigDecimal() ?: java.math.BigDecimal.ZERO
                                    )
                                }
                            )
                        )
                    }
                }
                is ApiResult.Error -> {}
            }
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    val tanggal = remember {
        SimpleDateFormat(
            "dd MMMM yyyy",
            Locale.forLanguageTag("id-ID")
        ).format(Date())
    }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = loadData,
        modifier = Modifier.fillMaxSize()
    ) {
        ProductionMainScreen(
            usernamePanduan = profil.username,
            dataBahanBaku = daftarBahan,
            dataBatch = daftarBatch,
            onSimpanBahanBaku = { b ->
                scope.launch {
                    val req = com.stb.sirabas.data.remote.RawMaterialRequest(
                        tanggal = b.tanggal,
                        jenis = b.jenis,
                        supplier = b.supplier,
                        asal_daerah = b.asalDaerah,
                        berat_masuk_kg = b.beratKg,
                        kadar_air = b.kadarAir,
                        derajat_sosoh = b.derajatSosoh,
                        broken = b.broken
                    )
                    when (val res = produksiRepository.createRawMaterial(req)) {
                        is ApiResult.Success -> {
                            onPesan("Bahan baku ${res.data.nomor_penerimaan} tersimpan di server.")
                            loadData()
                        }
                        is ApiResult.Error -> {
                            onPesan(parseApiErrorMessage(res.message))
                        }
                    }
                }
            },
            onSimpanBatch = { batch ->
                scope.launch {
                    val req = com.stb.sirabas.data.remote.ProductionRequest(
                        kode_batch = batch.kode,
                        tanggal = batch.tanggal,
                        nomor_penerimaan = batch.kodeBahan,
                        berat_input_kg = batch.beratInput.toDouble(),
                        hasil_utama = batch.rincian.hasilUtama.map { hu ->
                            val realKode = cariKodeProdukKatalog(hu.namaProduk, hu.kemasan, katalogProduk)
                            com.stb.sirabas.data.remote.ProductionOutputRequest(
                                kode_produk = realKode,
                                berat_kg = hu.beratKg.toDouble()
                            )
                        },
                        hasil_samping = batch.rincian.hasilSamping.map { hs ->
                            com.stb.sirabas.data.remote.SideOutputRequest(
                                nama_produk = hs.namaProduk,
                                berat_kg = hs.beratKg.toDouble()
                            )
                        }
                    )
                    when (val res = produksiRepository.createProductionBatch(req)) {
                        is ApiResult.Success -> {
                            onPesan("Batch produksi ${batch.kode} tersimpan di server.")
                            loadData()
                        }
                        is ApiResult.Error -> {
                            onPesan(parseApiErrorMessage(res.message))
                        }
                    }
                }
            },
            dataBeranda = ProductionHomeUi(
                namaPetugas = profil.namaLengkap,
                tanggal = tanggal,
                penerimaanHariIniKg = daftarBahan.sumOf { it.beratKg },
                jumlahPenerimaan = daftarBahan.size,
                stokBahanBakuKg = daftarBahan.sumOf { it.beratKg },
                jumlahTugasLabel = daftarBatch.size,
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
            halamanAkun = halamanAkun
        )
    }
}

private fun cariKodeProdukKatalog(
    nama: String,
    kemasan: String,
    katalog: List<com.stb.sirabas.data.remote.ProductDto>
): String {
    val cleanNama = nama.trim()
    val cleanKemasan = kemasan.trim().lowercase()

    val matched = katalog.firstOrNull { p ->
        p.nama.contains(cleanNama, ignoreCase = true) &&
                (cleanKemasan.isEmpty() || p.kemasan.contains(cleanKemasan.substringBefore(" "), ignoreCase = true))
    }
    if (matched != null) return matched.kode

    return when {
        cleanNama.contains("PREMIUM", ignoreCase = true) && cleanKemasan.contains("5") && !cleanKemasan.contains("25") -> "PREMIUM-5"
        cleanNama.contains("PREMIUM", ignoreCase = true) && cleanKemasan.contains("10") -> "PREMIUM-10"
        cleanNama.contains("PREMIUM", ignoreCase = true) && cleanKemasan.contains("hijau") -> "PREMIUM-25-HIJAU"
        cleanNama.contains("PREMIUM", ignoreCase = true) && cleanKemasan.contains("biru") -> "PREMIUM-25-BIRU"
        cleanNama.contains("MEDIUM", ignoreCase = true) -> "MEDIUM-25"
        cleanNama.contains("BERAS", ignoreCase = true) && cleanKemasan.contains("50") -> "BERAS-50"
        else -> cleanNama.uppercase().replace(" ", "-")
    }
}

private fun parseApiErrorMessage(msg: String): String {
    return if (msg.contains("{\"detail\":")) {
        val detail = msg.substringAfter("{\"detail\":\"").substringBefore("\"}")
        detail.ifBlank { msg }
    } else {
        msg
    }
}