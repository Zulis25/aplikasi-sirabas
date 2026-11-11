package com.stb.sirabas.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.math.BigInteger
import java.text.NumberFormat
import java.util.Locale
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date


data class PembayaranInvoiceUi(
    val waktu: String,
    val nominal: Long,
    val metode: String,
    val buktiTransfer: Uri? = null
)
data class InvoicePenjualanUi(
    val nomor: String,
    val tanggal: String,
    val pelanggan: String,
    val daftarItem: List<ItemPesananUi>,
    val jumlahDibayar: Long,
    val metodePembayaran: String?,
    val riwayatPembayaran: List<PembayaranInvoiceUi> = emptyList()
) {
    val total: BigInteger
        get() = daftarItem.fold(BigInteger.ZERO) { jumlah, item ->
            jumlah + item.hargaSatuan.toBigInteger() * item.jumlah.toBigInteger()
        }

    val sisaPiutang: BigInteger
        get() = (total - jumlahDibayar.toBigInteger()).max(BigInteger.ZERO)

    val lunas: Boolean
        get() = sisaPiutang == BigInteger.ZERO
}

@Composable
fun RiwayatPenjualanScreen(
    daftarInvoice: List<InvoicePenjualanUi>,
    namaPt: String,
    alamatPt: String,
    teleponPt: String,
    onUnduhGambar: ((InvoicePenjualanUi) -> Unit)? = null,
    onCetak: ((InvoicePenjualanUi) -> Unit)? = null
) {
    var pencarian by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("Semua") }
    var nomorTerpilih by rememberSaveable { mutableStateOf<String?>(null) }
    var halamanDetail by rememberSaveable { mutableStateOf("detail") }
    var pesan by remember { mutableStateOf<String?>(null) }

    // Setiap pembayaran disimpan terpisah agar bukti cicilan sebelumnya
    // tetap bisa dibuka setelah petugas mencatat pembayaran berikutnya.
    var pembayaranLokal by rememberSaveable {
        mutableStateOf(arrayListOf<android.os.Bundle>())
    }

    val context = LocalContext.current
    val localeIndonesia = remember { Locale.forLanguageTag("id-ID") }
    val formatAngka = remember {
        NumberFormat.getIntegerInstance(localeIndonesia)
    }

    fun rupiah(nilai: Number): String = "Rp${formatAngka.format(nilai)}"

    val pembayaranPerInvoice = pembayaranLokal.groupBy {
        it.getString("invoice")
    }

    val invoiceTerbaru = daftarInvoice.map { invoice ->
        val tambahan = pembayaranPerInvoice[invoice.nomor].orEmpty().map { data ->
            PembayaranInvoiceUi(
                waktu = data.getString("waktu").orEmpty(),
                nominal = data.getLong("nominal"),
                metode = data.getString("metode").orEmpty(),
                buktiTransfer = data.getString("bukti")?.let { Uri.parse(it) }
            )
        }

        if (tambahan.isEmpty()) {
            invoice
        } else {
            invoice.copy(
                jumlahDibayar = invoice.jumlahDibayar + tambahan.sumOf { it.nominal },
                metodePembayaran = tambahan.last().metode,
                riwayatPembayaran = invoice.riwayatPembayaran + tambahan
            )
        }
    }

    val terpilih = invoiceTerbaru.firstOrNull { it.nomor == nomorTerpilih }

    fun kembali() {
        if (halamanDetail == "detail") {
            nomorTerpilih = null
        } else {
            halamanDetail = "detail"
        }
    }

    BackHandler(enabled = terpilih != null) { kembali() }

    if (pesan != null) {
        AlertDialog(
            onDismissRequest = { pesan = null },
            title = { Text("Pemberitahuan") },
            text = { Text(pesan.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { pesan = null }) { Text("Tutup") }
            }
        )
    }

    if (terpilih != null) {
        when (halamanDetail) {
            "bayar" -> {
                fun simpan(nominal: Long, metode: String, bukti: Uri?) {
                    // Menahan ketukan kedua sebelum tampilan selesai berpindah.
                    if (halamanDetail != "bayar") return

                    if (nominal <= 0L || nominal.toBigInteger() > terpilih.sisaPiutang) {
                        pesan = "Periksa nominal dan sisa piutang invoice."
                        return
                    }

                    if (terpilih.jumlahDibayar > Long.MAX_VALUE - nominal) {
                        pesan = "Jumlah pembayaran melebihi batas pencatatan."
                        return
                    }

                    val waktu = SimpleDateFormat(
                        "dd MMMM yyyy, HH:mm",
                        localeIndonesia
                    ).format(Date())

                    val catatan = android.os.Bundle().apply {
                        putString("invoice", terpilih.nomor)
                        putString("waktu", waktu)
                        putLong("nominal", nominal)
                        putString("metode", metode)
                        if (metode == "Transfer Bank") {
                            putString("bukti", bukti?.toString())
                        }
                    }

                    pembayaranLokal = ArrayList(pembayaranLokal).apply {
                        add(catatan)
                    }
                    halamanDetail = "detail"
                }

                CatatPembayaranScreen(
                    invoice = terpilih,
                    onKembali = { kembali() },
                    onSimpan = { nominal, metode ->
                        simpan(nominal, metode, null)
                    },
                    onSimpanDenganBukti = { nominal, metode, uri ->
                        simpan(nominal, metode, uri)
                    }
                )
            }

            "riwayat" -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AccessColors.Background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        TextButton(onClick = { kembali() }) {
                            Text("‹  Kembali ke invoice", color = AccessColors.Green)
                        }
                    }

                    item {
                        Text(
                            "Riwayat pembayaran",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccessColors.Ink
                        )
                        Text(
                            "${terpilih.nomor} · ${terpilih.pelanggan}",
                            color = AccessColors.Muted
                        )
                    }

                    item {
                        Text(
                            "Total sudah dibayar ${rupiah(terpilih.jumlahDibayar)}",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Invoice lama bisa memiliki saldo dibayar tanpa rincian cicilan.
                    val totalRinci = terpilih.riwayatPembayaran.fold(
                        BigInteger.ZERO
                    ) { total, bayar -> total + bayar.nominal.toBigInteger() }

                    val dibayarSebelumnya =
                        terpilih.jumlahDibayar.toBigInteger() - totalRinci

                    if (dibayarSebelumnya.signum() > 0) {
                        item {
                            Text(
                                "Pembayaran sebelumnya ${rupiah(dibayarSebelumnya)} " +
                                        "· rincian belum tersedia",
                                color = AccessColors.Muted
                            )
                        }
                    }

                    if (terpilih.jumlahDibayar == 0L &&
                        terpilih.riwayatPembayaran.isEmpty()
                    ) {
                        item { Text("Belum ada pembayaran untuk invoice ini.") }
                    }

                    items(terpilih.riwayatPembayaran.asReversed()) { bayar ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    rupiah(bayar.nominal),
                                    color = AccessColors.Green,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(bayar.metode)
                                Text(bayar.waktu, color = AccessColors.Muted)

                                bayar.buktiTransfer?.let { uri ->
                                    TextButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(uri, "image/*")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            try {
                                                context.startActivity(intent)
                                            } catch (_: ActivityNotFoundException) {
                                                pesan = "Aplikasi pembuka gambar belum tersedia."
                                            } catch (_: SecurityException) {
                                                pesan = "Akses ke gambar bukti transfer sudah berakhir."
                                            }
                                        }
                                    ) {
                                        Text("Lihat bukti transfer")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                val simpanGambar = rememberSimpanGambarInvoice(
                    invoice = terpilih,
                    namaPt = namaPt,
                    alamatPt = alamatPt,
                    teleponPt = teleponPt
                )
                val cetakInvoice = rememberCetakInvoice(
                    invoice = terpilih,
                    namaPt = namaPt,
                    alamatPt = alamatPt,
                    teleponPt = teleponPt,
                    onGagal = { pesan = it }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AccessColors.Background)
                ) {
                    TextButton(onClick = { kembali() }) {
                        Text("‹  Kembali ke penjualan", color = AccessColors.Green)
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        DetailInvoiceScreen(
                            nomorInvoice = terpilih.nomor,
                            tanggalPesanan = terpilih.tanggal,
                            namaPt = namaPt,
                            alamatPt = alamatPt,
                            teleponPt = teleponPt,
                            namaPelanggan = terpilih.pelanggan,
                            daftarItem = terpilih.daftarItem,
                            jumlahDibayar = terpilih.jumlahDibayar,
                            metodePembayaran = terpilih.metodePembayaran,
                            onUnduhGambar = {
                                if (onUnduhGambar != null) onUnduhGambar(terpilih)
                                else simpanGambar()
                            },
                            onCetak = {
                                if (onCetak != null) onCetak(terpilih)
                                else cetakInvoice()
                            }
                        )
                    }

                    Surface(color = Color.White, shadowElevation = 3.dp) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (!terpilih.lunas) {
                                Button(
                                    onClick = { halamanDetail = "bayar" },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccessColors.Green
                                    )
                                ) {
                                    Text("Catat pembayaran")
                                }
                            }

                            TextButton(onClick = { halamanDetail = "riwayat" }) {
                                Text("Riwayat pembayaran", color = AccessColors.Green)
                            }
                        }
                    }
                }
            }
        }
        return
    }

    val kataKunci = pencarian.trim()
    val invoiceTampil = invoiceTerbaru.filter { invoice ->
        val cocokNamaAtauNomor =
            invoice.nomor.contains(kataKunci, ignoreCase = true) ||
                    invoice.pelanggan.contains(kataKunci, ignoreCase = true)

        val cocokStatus = when (filter) {
            "Lunas" -> invoice.lunas
            "Piutang" -> !invoice.lunas
            else -> true
        }
        cocokNamaAtauNomor && cocokStatus
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Penjualan",
                color = AccessColors.Ink,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = pencarian,
                onValueChange = { pencarian = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cari invoice atau pelanggan") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Semua", "Lunas", "Piutang").forEach { pilihan ->
                    FilterChip(
                        selected = filter == pilihan,
                        onClick = { filter = pilihan },
                        label = { Text(pilihan) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDDEFE4),
                            selectedLabelColor = Color(0xFF125139)
                        )
                    )
                }
            }
        }

        if (invoiceTampil.isEmpty()) {
            item {
                Text(
                    if (daftarInvoice.isEmpty()) "Belum ada invoice penjualan."
                    else "Tidak ada invoice yang cocok.",
                    color = AccessColors.Muted
                )
            }
        }

        items(invoiceTampil, key = { it.nomor }) { invoice ->
            Surface(
                onClick = {
                    nomorTerpilih = invoice.nomor
                    halamanDetail = "detail"
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                contentColor = AccessColors.Ink,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, AccessColors.Border
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(invoice.pelanggan, fontWeight = FontWeight.Bold)
                    Text("${invoice.nomor} · ${invoice.tanggal}", color = AccessColors.Muted)
                    Text("Total ${rupiah(invoice.total)}")
                    Text(
                        text = if (invoice.lunas) "Lunas"
                        else "Piutang · ${rupiah(invoice.sisaPiutang)}",
                        color = if (invoice.lunas) AccessColors.Green else Color(0xFF795300),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun RiwayatPenjualanScreen(
    daftarInvoice: List<InvoicePenjualanUi>,
    namaPt: String,
    alamatPt: String,
    teleponPt: String,
    onUnduhGambar: (InvoicePenjualanUi) -> Unit,
    onCetak: (InvoicePenjualanUi) -> Unit
) {
    var pencarian by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("Semua") }
    var nomorTerpilih by rememberSaveable { mutableStateOf<String?>(null) }

    val formatAngka = remember {
        NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"))
    }

    val invoiceTerpilih = daftarInvoice.firstOrNull {
        it.nomor == nomorTerpilih
    }

    BackHandler(enabled = invoiceTerpilih != null) {
        nomorTerpilih = null
    }

    // Ketika petugas mengetuk kartu, buka detail invoice yang dipilih.
    if (invoiceTerpilih != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AccessColors.Background)
        ) {
            TextButton(
                onClick = { nomorTerpilih = null },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("‹  Kembali ke penjualan", color = AccessColors.Green)
            }

            Box(modifier = Modifier.weight(1f)) {
                var pesanCetak by remember(invoiceTerpilih.nomor) {
                    mutableStateOf<String?>(null)
                }

                val simpanGambarRiwayat = rememberSimpanGambarInvoice(
                    invoice = invoiceTerpilih,
                    namaPt = namaPt,
                    alamatPt = alamatPt,
                    teleponPt = teleponPt
                )

                val cetakInvoiceRiwayat = rememberCetakInvoice(
                    invoice = invoiceTerpilih,
                    namaPt = namaPt,
                    alamatPt = alamatPt,
                    teleponPt = teleponPt,
                    onGagal = { keterangan -> pesanCetak = keterangan }
                )

                pesanCetak?.let { keterangan ->
                    AlertDialog(
                        onDismissRequest = { pesanCetak = null },
                        title = { Text("Invoice belum bisa dicetak") },
                        text = { Text(keterangan) },
                        confirmButton = {
                            TextButton(onClick = { pesanCetak = null }) {
                                Text("Tutup")
                            }
                        }
                    )
                }
                DetailInvoiceScreen(
                    nomorInvoice = invoiceTerpilih.nomor,
                    tanggalPesanan = invoiceTerpilih.tanggal,
                    namaPt = namaPt,
                    alamatPt = alamatPt,
                    teleponPt = teleponPt,
                    namaPelanggan = invoiceTerpilih.pelanggan,
                    daftarItem = invoiceTerpilih.daftarItem,
                    jumlahDibayar = invoiceTerpilih.jumlahDibayar,
                    metodePembayaran = invoiceTerpilih.metodePembayaran,
                    onUnduhGambar = simpanGambarRiwayat,
                    onCetak = cetakInvoiceRiwayat
                )
            }
        }
        return
    }

    val kataKunci = pencarian.trim()
    val invoiceTampil = daftarInvoice.filter { invoice ->
        val sesuaiPencarian =
            invoice.nomor.contains(kataKunci, ignoreCase = true) ||
                    invoice.pelanggan.contains(kataKunci, ignoreCase = true)

        val sesuaiStatus = when (filter) {
            "Lunas" -> invoice.lunas
            "Piutang" -> !invoice.lunas
            else -> true
        }

        sesuaiPencarian && sesuaiStatus
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Penjualan",
                color = AccessColors.Ink,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = pencarian,
                onValueChange = { pencarian = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cari invoice atau pelanggan") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Semua", "Lunas", "Piutang").forEach { pilihan ->
                    FilterChip(
                        selected = filter == pilihan,
                        onClick = { filter = pilihan },
                        label = { Text(pilihan) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDDEFE4),
                            selectedLabelColor = Color(0xFF125139)
                        )
                    )
                }
            }
        }

        if (invoiceTampil.isEmpty()) {
            item {
                Text(
                    text = if (daftarInvoice.isEmpty()) {
                        "Belum ada invoice penjualan."
                    } else {
                        "Tidak ada invoice yang cocok."
                    },
                    color = AccessColors.Muted,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }
        }

        items(invoiceTampil, key = { it.nomor }) { invoice ->
            Surface(
                onClick = { nomorTerpilih = invoice.nomor },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                contentColor = AccessColors.Ink,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, AccessColors.Border
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = invoice.pelanggan,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = invoice.nomor,
                                color = AccessColors.Muted,
                                fontSize = 13.sp
                            )
                        }

                        StatusInvoicePenjualan(lunas = invoice.lunas)
                    }

                    Text(
                        text = invoice.tanggal,
                        color = AccessColors.Muted,
                        fontSize = 13.sp
                    )

                    HorizontalDivider(color = AccessColors.Border)

                    Text(
                        text = "Total Rp${formatAngka.format(invoice.total)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    if (invoice.lunas) {
                        invoice.metodePembayaran?.let { metode ->
                            Text(
                                text = "Pembayaran · $metode",
                                color = AccessColors.Muted
                            )
                        }
                    } else {
                        Text(
                            text = "Sisa piutang Rp${formatAngka.format(invoice.sisaPiutang)}",
                            color = Color(0xFF815300),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusInvoicePenjualan(lunas: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (lunas) Color(0xFFE2F2E8) else Color(0xFFFFEFCB)
    ) {
        Text(
            text = if (lunas) "Lunas" else "Piutang",
            color = if (lunas) Color(0xFF176344) else Color(0xFF815300),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Preview(
    name = "Penjualan · Daftar invoice",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun RiwayatPenjualanPreview() {
    val beras = ProdukPenjualanUi(
        kode = "PREMIUM-25-HIJAU",
        nama = "Beras Premium",
        kemasan = "25 kg hijau",
        satuanJual = "karung",
        stokTersedia = 12,
        hargaKatalog = 350_000L
    )

    val daftarInvoice = listOf(
        InvoicePenjualanUi(
            nomor = "INV/2026/002",
            tanggal = "11 November 2026",
            pelanggan = "MBG B",
            daftarItem = listOf(ItemPesananUi(beras, 2, 350_000L)),
            jumlahDibayar = 200_000L,
            metodePembayaran = "Cash"
        ),
        InvoicePenjualanUi(
            nomor = "INV/2026/001",
            tanggal = "10 November 2026",
            pelanggan = "MBG A",
            daftarItem = listOf(ItemPesananUi(beras, 3, 350_000L)),
            jumlahDibayar = 1_050_000L,
            metodePembayaran = "Transfer Bank"
        )
    )

    SIRABASTheme {
        RiwayatPenjualanScreen(
            daftarInvoice = daftarInvoice,
            namaPt = "PT Sri Tani Berkah",
            alamatPt = "",
            teleponPt = "",
            onUnduhGambar = {},
            onCetak = {}
        )
    }
}