package com.stb.sirabas.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.math.BigInteger
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        get() = daftarItem.fold(BigInteger.ZERO) { subtotal, item ->
            subtotal + item.hargaSatuan.toBigInteger() * item.jumlah.toBigInteger()
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
    onCetak: ((InvoicePenjualanUi) -> Unit)? = null,
    onInvoiceDiperbarui: (InvoicePenjualanUi) -> Unit
) {
    val context = LocalContext.current
    val warna = MaterialTheme.colorScheme
    val locale = remember { Locale.forLanguageTag("id-ID") }
    val formatAngka = remember { NumberFormat.getIntegerInstance(locale) }

    var pencarian by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("Semua") }
    var nomorTerpilih by rememberSaveable { mutableStateOf<String?>(null) }
    var halaman by rememberSaveable { mutableStateOf("detail") }
    var pesan by rememberSaveable { mutableStateOf<String?>(null) }

    // Ambil invoice terbaru dari daftar yang juga dipakai oleh laporan.
    val invoice = daftarInvoice.firstOrNull { it.nomor == nomorTerpilih }

    fun rupiah(nilai: Number): String = "Rp${formatAngka.format(nilai)}"

    fun kembali() {
        if (halaman == "detail") {
            nomorTerpilih = null
        } else {
            halaman = "detail"
        }
    }

    BackHandler(enabled = invoice != null) { kembali() }

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

    if (invoice != null) {
        when (halaman) {
            "bayar" -> {
                fun simpan(nominal: Long, metode: String, bukti: Uri?) {
                    // Satu ketukan hanya boleh mencatat satu pembayaran.
                    if (halaman != "bayar") return

                    if (nominal <= 0L || nominal.toBigInteger() > invoice.sisaPiutang) {
                        pesan = "Periksa nominal dan sisa piutang invoice."
                        return
                    }

                    if (invoice.jumlahDibayar > Long.MAX_VALUE - nominal) {
                        pesan = "Jumlah pembayaran melebihi batas pencatatan."
                        return
                    }

                    val catatan = PembayaranInvoiceUi(
                        waktu = SimpleDateFormat(
                            "dd MMMM yyyy, HH:mm",
                            locale
                        ).format(Date()),
                        nominal = nominal,
                        metode = metode,
                        buktiTransfer = if (metode == "Transfer Bank") bukti else null
                    )

                    val invoiceBaru = invoice.copy(
                        jumlahDibayar = invoice.jumlahDibayar + nominal,
                        metodePembayaran = metode,
                        riwayatPembayaran = invoice.riwayatPembayaran + catatan
                    )

                    onInvoiceDiperbarui(invoiceBaru)
                    halaman = "detail"
                }

                CatatPembayaranScreen(
                    invoice = invoice,
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
                val totalRinci = invoice.riwayatPembayaran.fold(
                    BigInteger.ZERO
                ) { total, bayar ->
                    total + bayar.nominal.toBigInteger()
                }
                val saldoAwal = invoice.jumlahDibayar.toBigInteger() - totalRinci

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = warna.background,
                    contentColor = warna.onBackground
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            TextButton(onClick = { kembali() }) {
                                Text("‹  Kembali ke invoice")
                            }
                        }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(
                                    "Riwayat pembayaran",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("${invoice.nomor} · ${invoice.pelanggan}")
                                HorizontalDivider()
                            }
                        }
                        item {
                            Text(
                                "Sudah dibayar ${rupiah(invoice.jumlahDibayar)}",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("Sisa piutang ${rupiah(invoice.sisaPiutang)}")
                        }

                        if (saldoAwal.signum() > 0) {
                            item {
                                Text(
                                    "Pembayaran sebelumnya ${rupiah(saldoAwal)} " +
                                            "· rincian belum tersedia",
                                    color = warna.onSurfaceVariant
                                )
                            }
                        }

                        if (invoice.jumlahDibayar == 0L &&
                            invoice.riwayatPembayaran.isEmpty()
                        ) {
                            item { Text("Belum ada pembayaran untuk invoice ini.") }
                        }

                        items(invoice.riwayatPembayaran.asReversed()) { bayar ->
                            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        rupiah(bayar.nominal),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(bayar.metode)
                                    Text(bayar.waktu, color = warna.onSurfaceVariant)

                                    bayar.buktiTransfer?.let { uri ->
                                        TextButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW)
                                                    .setDataAndType(uri, "image/*")
                                                    .addFlags(
                                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                    )
                                                try {
                                                    context.startActivity(intent)
                                                } catch (_: ActivityNotFoundException) {
                                                    pesan = "Aplikasi pembuka gambar tidak tersedia."
                                                } catch (_: SecurityException) {
                                                    pesan = "Akses ke bukti transfer sudah berakhir."
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
            }

            else -> {
                val simpanGambar = rememberSimpanGambarInvoice(
                    invoice = invoice,
                    namaPt = namaPt,
                    alamatPt = alamatPt,
                    teleponPt = teleponPt
                )
                val cetakInvoice = rememberCetakInvoice(
                    invoice = invoice,
                    namaPt = namaPt,
                    alamatPt = alamatPt,
                    teleponPt = teleponPt,
                    onGagal = { pesan = it }
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = warna.background
                ) {
                    Column {
                        TextButton(onClick = { kembali() }) {
                            Text("‹  Kembali ke penjualan")
                        }

                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            DetailInvoiceScreen(
                                nomorInvoice = invoice.nomor,
                                tanggalPesanan = invoice.tanggal,
                                namaPt = namaPt,
                                alamatPt = alamatPt,
                                teleponPt = teleponPt,
                                namaPelanggan = invoice.pelanggan,
                                daftarItem = invoice.daftarItem,
                                jumlahDibayar = invoice.jumlahDibayar,
                                metodePembayaran = invoice.metodePembayaran,
                                onUnduhGambar = {
                                    if (onUnduhGambar != null) {
                                        onUnduhGambar(invoice)
                                    } else {
                                        simpanGambar()
                                    }
                                },
                                onCetak = {
                                    if (onCetak != null) {
                                        onCetak(invoice)
                                    } else {
                                        cetakInvoice()
                                    }
                                }
                            )
                        }

                        Surface(color = warna.surface, shadowElevation = 3.dp) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (!invoice.lunas) {
                                    Button(
                                        onClick = { halaman = "bayar" },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Catat pembayaran")
                                    }
                                }
                                TextButton(onClick = { halaman = "riwayat" }) {
                                    Text("Riwayat pembayaran")
                                }
                            }
                        }
                    }
                }
            }
        }
        return
    }

    val kataKunci = pencarian.trim()
    val invoiceTampil = daftarInvoice.filter { item ->
        val cocokPencarian = item.nomor.contains(kataKunci, ignoreCase = true) ||
                item.pelanggan.contains(kataKunci, ignoreCase = true)

        val cocokStatus = when (filter) {
            "Lunas" -> item.lunas
            "Piutang" -> !item.lunas
            else -> true
        }

        cocokPencarian && cocokStatus
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = warna.background,
        contentColor = warna.onBackground
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Penjualan", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
            }

            item {
                OutlinedTextField(
                    value = pencarian,
                    onValueChange = { pencarian = it },
                    label = { Text("Cari invoice atau pelanggan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Semua", "Lunas", "Piutang").forEach { pilihan ->
                        FilterChip(
                            selected = filter == pilihan,
                            onClick = { filter = pilihan },
                            label = { Text(pilihan) }
                        )
                    }
                }
            }

            if (invoiceTampil.isEmpty()) {
                item {
                    Text(
                        if (daftarInvoice.isEmpty()) "Belum ada invoice penjualan."
                        else "Tidak ada invoice yang cocok."
                    )
                }
            }

            items(invoiceTampil, key = { it.nomor }) { item ->
                OutlinedCard(
                    onClick = {
                        nomorTerpilih = item.nomor
                        halaman = "detail"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(item.pelanggan, fontWeight = FontWeight.Bold)
                        Text(
                            "${item.nomor} · ${item.tanggal}",
                            color = warna.onSurfaceVariant
                        )
                        Text("Total ${rupiah(item.total)}")
                        Text(
                            text = if (item.lunas) "Lunas"
                            else "Piutang · tersisa ${rupiah(item.sisaPiutang)}",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RiwayatPenjualanPreview() {
    val contohProduk = ProdukPenjualanUi(
        kode = "PREMIUM-5",
        nama = "Beras Premium",
        kemasan = "5 kg",
        satuanJual = "karung",
        stokTersedia = 10,
        hargaKatalog = 75_000L
    )

    var contohInvoice by remember {
        mutableStateOf(
            InvoicePenjualanUi(
                nomor = "INV/2026/002",
                tanggal = "18 November 2026",
                pelanggan = "MBG B",
                daftarItem = listOf(ItemPesananUi(contohProduk, 1, 75_000L)),
                jumlahDibayar = 50_000L,
                metodePembayaran = "Cash"
            )
        )
    }

    SIRABASTheme {
        RiwayatPenjualanScreen(
            daftarInvoice = listOf(contohInvoice),
            namaPt = "PT. Sri Tani Berkah",
            alamatPt = "",
            teleponPt = "",
            onInvoiceDiperbarui = { contohInvoice = it }
        )
    }
}