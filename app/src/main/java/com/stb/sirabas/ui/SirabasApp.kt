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
private const val ALAMAT_KANTOR = "Jl. Poros Makassar - Maros Km. 20, Kab. Maros, Sulawesi Selatan"
private const val TELEPON_KANTOR = "0812-4211-8888"

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

    val pengajuan = remember(context) {
        context.getSharedPreferences(
            "sirabas_pengajuan_akun",
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

            pengajuan.contains("username") ||
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

    var namaPengajuan by remember {
        mutableStateOf(pengajuan.getString("nama", "").orEmpty())
    }
    var usernamePengajuan by remember {
        mutableStateOf(pengajuan.getString("username", "").orEmpty())
    }
    var whatsappPengajuan by remember {
        mutableStateOf(pengajuan.getString("whatsapp", "").orEmpty())
    }
    var statusAktivasiAkun by remember {
        mutableStateOf(
            pengajuan.getString("status", "MENUNGGU_AKTIVASI")
                ?: "MENUNGGU_AKTIVASI"
        )
    }

// Hanya dipakai selama aplikasi masih terbuka.
    var lastPassword by remember { mutableStateOf("") }

    fun simpanPengajuan(
        nama: String,
        username: String,
        whatsapp: String,
        status: String
    ) {
        namaPengajuan = nama
        usernamePengajuan = username.trim()
        whatsappPengajuan = whatsapp
        statusAktivasiAkun = status

        pengajuan.edit()
            .putString("nama", namaPengajuan)
            .putString("username", usernamePengajuan)
            .putString("whatsapp", whatsappPengajuan)
            .putString("status", statusAktivasiAkun)
            .apply()
    }

    fun hapusPengajuanJikaMilik(username: String) {
        if (usernamePengajuan == username.trim()) {
            pengajuan.edit().clear().apply()
            namaPengajuan = ""
            usernamePengajuan = ""
            whatsappPengajuan = ""
            statusAktivasiAkun = "MENUNGGU_AKTIVASI"
            lastPassword = ""
        }
    }

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

    val sedangDiDashboard = layar == "produksi" || layar == "penjualan"

    SIRABASTheme(
        darkTheme = sedangDiDashboard && modeGelap,
        dynamicColor = false
    ) {
        if (tampilkanSplash) {
            SplashScreen()
        } else {
            when (layar) {
                "onboarding" -> OnboardingScreen(
                    onSelesai = { layar = "login" }
                )

                "login" -> LoginScreen(
                    onDaftar = {
                        layar = if (usernamePengajuan.isBlank()) {
                            "register"
                        } else {
                            "menunggu_aktivasi"
                        }
                    },
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
                                            val pengguna = res.data
                                            val usernameServer = pengguna.username
                                            val peranServer = pengguna.role.orEmpty().trim().lowercase(Locale.ROOT)

                                            when {
                                                peranServer !in setOf("produksi", "penjualan") -> {
                                                    authRepository.logout()
                                                    pesan = "Akses aplikasi mobile tersedia untuk petugas Produksi dan Penjualan."
                                                }

                                                usernamePengajuan.equals(usernameServer, ignoreCase = true) &&
                                                        statusAktivasiAkun == "MENUNGGU_AKTIVASI" -> {
                                                    authRepository.logout()

                                                    simpanPengajuan(
                                                        nama = pengguna.nama_lengkap ?: namaPengajuan,
                                                        username = usernameServer,
                                                        whatsapp = whatsappPengajuan,
                                                        status = "AKTIF"
                                                    )
                                                    lastPassword = ""
                                                    layar = "menunggu_aktivasi"
                                                }

                                                else -> {
                                                    sesiLogin.edit()
                                                        .putString(
                                                            "nama_lengkap",
                                                            pengguna.nama_lengkap ?: usernameServer
                                                        )
                                                        .apply()

                                                    hapusPengajuanJikaMilik(usernameServer)
                                                    masukKeDashboard(usernameServer, peranServer)
                                                }
                                            }
                                        }

                                        is AuthResult.PendingActivation -> {
                                            val pengajuanYangSama = usernamePengajuan == nama

                                            simpanPengajuan(
                                                nama = if (pengajuanYangSama) namaPengajuan else "",
                                                username = nama,
                                                whatsapp = if (pengajuanYangSama) whatsappPengajuan else "",
                                                status = "MENUNGGU_AKTIVASI"
                                            )

                                            lastPassword = sandi
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
                                    simpanPengajuan(
                                        nama = nama,
                                        username = username,
                                        whatsapp = whatsapp,
                                        status = "MENUNGGU_AKTIVASI"
                                    )
                                    lastPassword = password
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

                "menunggu_aktivasi" -> {
                    var permintaanCek by remember { mutableStateOf(0) }
                    val jendelaAktif = androidx.compose.ui.platform.LocalWindowInfo
                        .current.isWindowFocused

                    LaunchedEffect(
                        usernamePengajuan,
                        lastPassword,
                        statusAktivasiAkun,
                        permintaanCek,
                        jendelaAktif
                    ) {
                        if (
                            !jendelaAktif ||
                            usernamePengajuan.isBlank() ||
                            lastPassword.isBlank() ||
                            statusAktivasiAkun != "MENUNGGU_AKTIVASI"
                        ) {
                            return@LaunchedEffect
                        }

                        // Saat halaman pertama dibuka, beri jeda sebelum memeriksa.
                        // Tombol "Cek status aktivasi" memicu pemeriksaan langsung.
                        if (permintaanCek == 0) delay(15_000L)

                        while (true) {
                            when (
                                val hasil = authRepository.login(
                                    usernamePengajuan,
                                    lastPassword
                                )
                            ) {
                                is AuthResult.Success -> {
                                    // Pemeriksaan status menggunakan endpoint login.
                                    // Sesi dashboard dimulai saat pengguna login sendiri.
                                    authRepository.logout()

                                    simpanPengajuan(
                                        nama = hasil.data.nama_lengkap ?: namaPengajuan,
                                        username = hasil.data.username,
                                        whatsapp = whatsappPengajuan,
                                        status = "AKTIF"
                                    )
                                    lastPassword = ""
                                    break
                                }

                                is AuthResult.PendingActivation -> {
                                    // Server masih menyatakan pengajuan menunggu.
                                }

                                is AuthResult.Error -> {
                                    // Pertahankan status terakhir jika koneksi gagal.
                                    pesan = hasil.message
                                    break
                                }
                            }

                            delay(15_000L)
                        }
                    }

                    PendingActivationScreen(
                        nama = namaPengajuan,
                        username = usernamePengajuan,
                        whatsapp = whatsappPengajuan,
                        statusAktivasi = statusAktivasiAkun,
                        onCekStatus = {
                            if (lastPassword.isBlank()) {
                                kembaliKeLogin()
                                pesan = "Masukkan username dan kata sandi untuk memeriksa aktivasi akun."
                            } else {
                                permintaanCek++
                            }
                        },
                        onMasuk = kembaliKeLogin
                    )
                }

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
                        namaPerusahaan = p.nama_perusahaan?.ifBlank { null } ?: PERUSAHAAN,
                        alamatPerusahaan = ALAMAT_KANTOR,
                        teleponPerusahaan = TELEPON_KANTOR
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
                                namaPerusahaan = p.nama_perusahaan?.ifBlank { null } ?: PERUSAHAAN,
                                alamatPerusahaan = ALAMAT_KANTOR,
                                teleponPerusahaan = TELEPON_KANTOR
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
                                val foto = when {
                                    p.kode.contains("PREMIUM-5", ignoreCase = true) -> R.drawable.premium_5kg
                                    p.kode.contains("PREMIUM-10", ignoreCase = true) -> R.drawable.premium_10kg
                                    p.kode.contains("PREMIUM-25-HIJAU", ignoreCase = true) -> R.drawable.premium_25kg_hijau
                                    p.kode.contains("PREMIUM-25-BIRU", ignoreCase = true) -> R.drawable.premium_25kg_biru
                                    p.kode.contains("MEDIUM-25", ignoreCase = true) -> R.drawable.medium_25kg
                                    p.kode.contains("BERAS-50", ignoreCase = true) -> R.drawable.beras_50kg
                                    p.nama.contains("PREMIUM", ignoreCase = true) && p.kemasan.contains("5") -> R.drawable.premium_5kg
                                    p.nama.contains("PREMIUM", ignoreCase = true) && p.kemasan.contains("10") -> R.drawable.premium_10kg
                                    p.nama.contains("PREMIUM", ignoreCase = true) && p.kemasan.contains("hijau") -> R.drawable.premium_25kg_hijau
                                    p.nama.contains("PREMIUM", ignoreCase = true) && p.kemasan.contains("biru") -> R.drawable.premium_25kg_biru
                                    p.nama.contains("MEDIUM", ignoreCase = true) -> R.drawable.medium_25kg
                                    p.nama.contains("BERAS", ignoreCase = true) && p.kemasan.contains("50") -> R.drawable.beras_50kg
                                    else -> R.drawable.premium_25kg_hijau
                                }
                                val imgUrl = p.gambar_path?.takeIf { it.isNotBlank() }?.let { path ->
                                    if (path.startsWith("http")) path else "https://aryaganteng.sovereigns.my.id$path"
                                }
                                ProdukPenjualanUi(
                                    kode = p.kode,
                                    nama = p.nama,
                                    kemasan = p.kemasan,
                                    satuanJual = "karung",
                                    stokTersedia = p.stok_tersedia,
                                    hargaKatalog = p.harga_katalog,
                                    fotoRes = foto,
                                    fotoUrl = imgUrl
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
                            val itemsMapped = inv.items.orEmpty().map { item ->
                                val price = item.harga ?: item.harga_satuan ?: item.subtotal?.let { if (item.jumlah > 0) it / item.jumlah else 0L } ?: 0L
                                ItemPesananUi(
                                    produk = ProdukPenjualanUi(
                                        kode = item.kode_produk.orEmpty(),
                                        nama = item.nama_produk.orEmpty().ifBlank { item.kode_produk.orEmpty() },
                                        kemasan = item.kemasan.orEmpty().ifBlank { "karung" },
                                        satuanJual = "karung",
                                        stokTersedia = 99,
                                        hargaKatalog = price
                                    ),
                                    jumlah = item.jumlah,
                                    hargaSatuan = price
                                )
                            }
                            val paid = if (inv.jumlah_dibayar > 0) inv.jumlah_dibayar else inv.pembayaran.orEmpty().sumOf { it.jumlah }
                            InvoicePenjualanUi(
                                nomor = inv.nomor_invoice,
                                tanggal = inv.tanggal.orEmpty(),
                                pelanggan = inv.pelanggan.orEmpty(),
                                daftarItem = itemsMapped,
                                jumlahDibayar = paid,
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
        while (true) {
            loadData()
            delay(5000)
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
                                if (newInv.jumlahDibayar > 0) {
                                    penjualanRepository.addPayment(
                                        nomorInvoice = newInv.nomor,
                                        jumlah = newInv.jumlahDibayar,
                                        metodeBayar = newInv.metodePembayaran ?: "TUNAI"
                                    )
                                }
                                onInvoiceBaru(newInv)
                                onPesan("Invoice ${newInv.nomor} berhasil dibuat di server.")
                                loadData()
                            }
                            is ApiResult.Error -> {
                                onPesan(parseApiErrorMessage(res.message))
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
                onInvoiceDiperbarui = { invoiceBaru ->
                    val indeks = daftarInvoice.indexOfFirst { it.nomor == invoiceBaru.nomor }
                    if (indeks >= 0) {
                        val invoiceLama = daftarInvoice[indeks]
                        val selisih = invoiceBaru.jumlahDibayar - invoiceLama.jumlahDibayar
                        if (selisih > 0) {
                            scope.launch {
                                when (val res = penjualanRepository.addPayment(
                                    nomorInvoice = invoiceBaru.nomor,
                                    jumlah = selisih,
                                    metodeBayar = invoiceBaru.metodePembayaran ?: "TUNAI"
                                )) {
                                    is ApiResult.Success -> {
                                        daftarInvoice[indeks] = invoiceBaru
                                        onPesan("Pembayaran untuk ${invoiceBaru.nomor} berhasil dicatat di server.")
                                        loadData()
                                    }
                                    is ApiResult.Error -> {
                                        onPesan(parseApiErrorMessage(res.message))
                                    }
                                }
                            }
                        } else {
                            daftarInvoice[indeks] = invoiceBaru
                        }
                    }
                }
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
    var pesananProduksi by remember { mutableStateOf<List<ProductionOrderUi>>(emptyList()) }
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

            when (val resOrders = produksiRepository.getProductionOrders()) {
                is ApiResult.Success -> {
                    val ordersMap = resOrders.data.orEmpty().groupBy { it.nomor_invoice }
                    pesananProduksi = ordersMap.map { (nomorInvoice, orderItems) ->
                        val first = orderItems.first()
                        val item1 = orderItems.firstOrNull()
                        val (namaProd, kemasanProd) = uraiKodeProduk(item1?.kode_produk.orEmpty(), katalogProduk)
                        val beratKg = (item1?.kemasan ?: kemasanProd)
                            .trim()
                            .substringBefore(" ")
                            .toIntOrNull() ?: 25

                        ProductionOrderUi(
                            nomorPesanan = nomorInvoice,
                            namaPelanggan = first.pelanggan.orEmpty().ifBlank { "Pelanggan" },
                            namaProduk = item1?.nama_produk ?: namaProd,
                            beratPerKarungKg = beratKg,
                            jumlahKarung = item1?.jumlah_sisa ?: item1?.jumlah_diminta ?: 1,
                            kemasan = item1?.kemasan ?: kemasanProd,
                            tanggalPesanan = first.tanggal ?: first.dibuat_pada?.take(10) ?: "Hari ini"
                        )
                    }
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
                            beratKg = raw.berat_masuk_kg ?: 0.0,
                            sisaKg = raw.sisa_kg,
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
                        val listUtama = b.hasil_utama.orEmpty()
                        val totalUtama = b.total_hasil_utama_kg ?: 0.0

                        val hasilUtamaParsed = if (listUtama.isEmpty() && totalUtama > 0) {
                            listOf(
                                HasilUtamaProduksiUi(
                                    namaProduk = "Beras Premium",
                                    kemasan = "25 kg hijau",
                                    beratKg = totalUtama.toBigDecimal()
                                )
                            )
                        } else {
                            listUtama.map { hu ->
                                val kodeProduk = hu.kode_produk.orEmpty().trim()
                                val (namaProduk, kemasan) = uraiKodeProduk(kodeProduk, katalogProduk)

                                HasilUtamaProduksiUi(
                                    namaProduk = namaProduk,
                                    kemasan = if (kemasan.isBlank()) "25 kg" else kemasan,
                                    beratKg = hu.berat_kg?.toBigDecimal()
                                        ?: java.math.BigDecimal.ZERO
                                )
                            }
                        }

                        val listSamping = b.hasil_samping.orEmpty()
                        val totalSamping = b.total_hasil_samping_kg ?: 0.0

                        val hasilSampingParsed = if (listSamping.isEmpty() && totalSamping > 0) {
                            listOf(
                                HasilSampingProduksiUi(
                                    namaProduk = "Dedak halus",
                                    beratKg = totalSamping.toBigDecimal()
                                )
                            )
                        } else {
                            listSamping.map { hs ->
                                HasilSampingProduksiUi(
                                    namaProduk = hs.nama_produk ?: "Dedak halus",
                                    beratKg = hs.berat_kg?.toBigDecimal() ?: java.math.BigDecimal.ZERO
                                )
                            }
                        }

                        BatchProduksiUi(
                            kode = b.kode_batch,
                            tanggal = b.tanggal ?: "",
                            kodeBahan = b.nomor_penerimaan ?: "",
                            beratInput = b.berat_input_kg?.toBigDecimal() ?: java.math.BigDecimal.ZERO,
                            rincian = RincianHasilProduksiUi(
                                hasilUtama = hasilUtamaParsed,
                                hasilSamping = hasilSampingParsed
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
        while (true) {
            loadData()
            delay(5000)
        }
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
                    try {
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
                    } catch (e: java.io.IOException) {
                        onPesan("Koneksi terputus. Periksa riwayat penerimaan sebelum mencoba lagi.")
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
                                nama_produk = when {
                                    hs.namaProduk.trim().equals("Dedak Halus", ignoreCase = true) -> "Dedak halus"
                                    hs.namaProduk.trim().equals("Dedak Kasar", ignoreCase = true) -> "Dedak kasar"
                                    else -> hs.namaProduk.trim()
                                },
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
            dataBeranda = run {
                val hariIniIso = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
                val penerimaanHariIni = daftarBahan.filter { it.tanggal == hariIniIso }

                ProductionHomeUi(
                    namaPetugas = profil.namaLengkap,
                    tanggal = tanggal,
                    penerimaanHariIniKg = penerimaanHariIni.sumOf { it.beratKg },
                    jumlahPenerimaan = penerimaanHariIni.size,
                    stokBahanBakuKg = daftarBahan.sumOf { it.sisaLot },
                    jumlahTugasLabel = daftarBatch.size,
                    drafPenerimaanKg = 0.0,
                    pesanan = if (pesananProduksi.isNotEmpty()) {
                        pesananProduksi
                    } else {
                        daftarInvoice.flatMap { invoice ->
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
                    }
                )
            },
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

private fun uraiKodeProduk(
    kodeProduk: String,
    katalog: List<com.stb.sirabas.data.remote.ProductDto>
): Pair<String, String> {
    val cleanKode = kodeProduk.trim().uppercase()
    val matched = katalog.firstOrNull { it.kode.trim().equals(cleanKode, ignoreCase = true) }
    if (matched != null) {
        return matched.nama to matched.kemasan
    }
    return when {
        cleanKode.contains("PREMIUM-25-HIJAU") -> "Beras Premium" to "25 kg hijau"
        cleanKode.contains("PREMIUM-25-BIRU") -> "Beras Premium" to "25 kg biru"
        cleanKode.contains("PREMIUM-5") -> "Beras Premium" to "5 kg"
        cleanKode.contains("PREMIUM-10") -> "Beras Premium" to "10 kg"
        cleanKode.contains("MEDIUM-25") -> "Beras Medium" to "25 kg"
        cleanKode.contains("BERAS-50") -> "Beras" to "50 kg"
        cleanKode.contains("PREMIUM") -> "Beras Premium" to "25 kg"
        cleanKode.contains("MEDIUM") -> "Beras Medium" to "25 kg"
        else -> cleanKode to "25 kg"
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