package com.stb.sirabas.ui.screens

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.stb.sirabas.ui.theme.SIRABASTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.math.BigInteger
import java.text.NumberFormat
import java.util.Locale

// Titik masuk checkout untuk layar penjualan yang menyimpan invoice.
@Composable
fun CheckoutPenjualanDialog(
    nomorInvoice: String,
    namaPt: String,
    alamatPt: String,
    teleponPt: String,
    tanggalPesanan: String,
    keranjang: List<ItemPesananUi>,
    daftarPelanggan: List<String>,
    onTutup: () -> Unit,
    onInvoiceDibuat: (InvoicePenjualanUi) -> Unit
) {
    var sudahDikirim by remember(nomorInvoice) { mutableStateOf(false) }

    CheckoutInvoiceDialogDenganBukti(
        namaPt = namaPt,
        alamatPt = alamatPt,
        teleponPt = teleponPt,
        tanggalPesanan = tanggalPesanan,
        keranjang = keranjang,
        daftarPelanggan = daftarPelanggan,
        onTutup = onTutup,
        onKonfirmasi = { pelanggan, item, status, metode, bukti ->
            if (!sudahDikirim) {
                val invoice = InvoicePenjualanUi(
                    nomor = nomorInvoice,
                    tanggal = tanggalPesanan,
                    pelanggan = pelanggan,
                    daftarItem = item,
                    jumlahDibayar = 0L,
                    metodePembayaran = metode
                )
                val hasil = if (status == "Lunas") {
                    val bayar = PembayaranInvoiceUi(
                        waktu = java.text.DateFormat.getDateTimeInstance(
                            java.text.DateFormat.MEDIUM,
                            java.text.DateFormat.SHORT,
                            Locale.forLanguageTag("id-ID")
                        ).format(java.util.Date()),
                        nominal = invoice.total.toLong(),
                        metode = requireNotNull(metode),
                        buktiTransfer = bukti
                    )
                    invoice.copy(
                        jumlahDibayar = bayar.nominal,
                        riwayatPembayaran = listOf(bayar)
                    )
                } else {
                    invoice
                }
                sudahDikirim = true
                onInvoiceDibuat(hasil)
            }
        }
    )
}

// Pemanggilan lama dari Beranda tetap bisa digunakan.
@Composable
fun CheckoutInvoiceDialog(
    namaPt: String,
    alamatPt: String,
    teleponPt: String,
    tanggalPesanan: String,
    keranjang: List<ItemPesananUi>,
    daftarPelanggan: List<String>,
    onTutup: () -> Unit,
    onKonfirmasi: (
        pelanggan: String,
        itemFinal: List<ItemPesananUi>,
        status: String,
        metode: String?
    ) -> Unit
) {
    CheckoutInvoiceDialogDenganBukti(
        namaPt = namaPt,
        alamatPt = alamatPt,
        teleponPt = teleponPt,
        tanggalPesanan = tanggalPesanan,
        keranjang = keranjang,
        daftarPelanggan = daftarPelanggan,
        onTutup = onTutup,
        onKonfirmasi = { pelanggan, item, status, metode, _ ->
            onKonfirmasi(pelanggan, item, status, metode)
        }
    )
}

// Gunakan fungsi ini saat layar pemanggil sudah siap menerima bukti.
@Composable
fun CheckoutInvoiceDialogDenganBukti(
    namaPt: String,
    alamatPt: String,
    teleponPt: String,
    tanggalPesanan: String,
    keranjang: List<ItemPesananUi>,
    daftarPelanggan: List<String>,
    onTutup: () -> Unit,
    onKonfirmasi: (
        pelanggan: String,
        itemFinal: List<ItemPesananUi>,
        status: String,
        metode: String?,
        buktiTransfer: Uri?
    ) -> Unit
) {
    Dialog(onDismissRequest = onTutup) {
        IsiCheckoutInvoice(
            namaPt = namaPt,
            alamatPt = alamatPt,
            teleponPt = teleponPt,
            tanggalPesanan = tanggalPesanan,
            keranjang = keranjang,
            daftarPelanggan = daftarPelanggan,
            onTutup = onTutup,
            onKonfirmasi = onKonfirmasi
        )
    }
}

@Composable
private fun IsiCheckoutInvoice(
    namaPt: String,
    alamatPt: String,
    teleponPt: String,
    tanggalPesanan: String,
    keranjang: List<ItemPesananUi>,
    daftarPelanggan: List<String>,
    onTutup: () -> Unit,
    onKonfirmasi: (String, List<ItemPesananUi>, String, String?, Uri?) -> Unit,
    metodeAwal: String = "Cash"
) {
    val hijau = Color(0xFF176344)
    val format = remember {
        NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"))
    }

    var pelanggan by rememberSaveable { mutableStateOf("") }
    var pilihanTerbuka by remember { mutableStateOf(false) }
    var status by rememberSaveable { mutableStateOf("Lunas") }
    var metode by rememberSaveable { mutableStateOf(metodeAwal) }
    var buktiUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    var hargaInput by rememberSaveable(keranjang) {
        mutableStateOf(ArrayList(keranjang.map { it.hargaSatuan.toString() }))
    }

    val hargaValid = hargaInput.map { it.toLongOrNull() }
    val totalValid = keranjang.isNotEmpty() &&
            keranjang.all { it.jumlah > 0 } &&
            hargaValid.all { it != null && it > 0L }

    val total = keranjang.foldIndexed(BigInteger.ZERO) { index, jumlah, item ->
        jumlah + (hargaValid[index] ?: 0L).toBigInteger() *
                item.jumlah.toBigInteger()
    }

    val transferDipilih = status == "Lunas" && metode == "Transfer Bank"

    Surface(
        modifier = Modifier.fillMaxWidth().heightIn(max = 720.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        contentColor = AccessColors.Ink
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Periksa invoice",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(namaPt, fontWeight = FontWeight.Bold, color = hijau)
                    if (alamatPt.isNotBlank()) Text(alamatPt)
                    if (teleponPt.isNotBlank()) Text("Telp. $teleponPt")
                    Text("Tanggal pesanan · $tanggalPesanan")
                }

                HorizontalDivider()

                Box {
                    OutlinedTextField(
                        value = pelanggan,
                        onValueChange = { pelanggan = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nama pelanggan") },
                        singleLine = true,
                        trailingIcon = {
                            TextButton(onClick = { pilihanTerbuka = true }) {
                                Text("Pilih", color = hijau)
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = pilihanTerbuka,
                        onDismissRequest = { pilihanTerbuka = false }
                    ) {
                        if (daftarPelanggan.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Belum ada pelanggan tersimpan") },
                                onClick = {},
                                enabled = false
                            )
                        }

                        daftarPelanggan.forEach { nama ->
                            DropdownMenuItem(
                                text = { Text(nama) },
                                onClick = {
                                    pelanggan = nama
                                    pilihanTerbuka = false
                                }
                            )
                        }
                    }
                }

                Text("Rincian pesanan", fontWeight = FontWeight.Bold)

                keranjang.forEachIndexed { index, item ->
                    val produk = item.produk

                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            "${produk.nama} · ${produk.kemasan}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("${item.jumlah} ${produk.satuanJual}")

                        OutlinedTextField(
                            value = hargaInput[index],
                            onValueChange = { baru ->
                                if (baru.length <= 18 && baru.all { it in '0'..'9' }) {
                                    hargaInput = ArrayList(hargaInput).apply {
                                        this[index] = baru
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Harga per ${produk.satuanJual}") },
                            prefix = { Text("Rp ") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            isError = (hargaValid[index] ?: 0L) <= 0L
                        )
                    }
                }

                HorizontalDivider()
                Text("Status pembayaran", fontWeight = FontWeight.SemiBold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Lunas", "Piutang").forEach { pilihan ->
                        PilihanInvoice(pilihan, status == pilihan) {
                            status = pilihan
                            if (pilihan == "Piutang") buktiUri = null
                        }
                    }
                }

                if (status == "Lunas") {
                    Text("Metode pembayaran", fontWeight = FontWeight.SemiBold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Cash", "Transfer Bank").forEach { pilihan ->
                            PilihanInvoice(pilihan, metode == pilihan) {
                                metode = pilihan
                                if (pilihan == "Cash") buktiUri = null
                            }
                        }
                    }

                    if (transferDipilih) {
                        BagianBuktiTransfer(
                            uri = buktiUri,
                            onGambarBerubah = { buktiUri = it }
                        )
                    }
                } else if (totalValid) {
                    Text(
                        text = "Sisa piutang Rp${format.format(total)}",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total invoice", fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (totalValid) "Rp${format.format(total)}" else "—",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = hijau
                )
            }

            Spacer(Modifier.height(8.dp))

            if (total > BigInteger.valueOf(Long.MAX_VALUE)) {
                Text("Total melebihi batas nominal invoice.", color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val itemFinal = keranjang.mapIndexed { index, item ->
                        item.copy(hargaSatuan = hargaInput[index].toLong())
                    }

                    onKonfirmasi(
                        pelanggan.trim(),
                        itemFinal,
                        status,
                        metode.takeIf { status == "Lunas" },
                        buktiUri.takeIf { transferDipilih }
                    )
                },
                enabled = pelanggan.isNotBlank() && totalValid &&
                        total <= BigInteger.valueOf(Long.MAX_VALUE),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = hijau)
            ) {
                Text("Konfirmasi invoice")
            }

            TextButton(
                onClick = onTutup,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Kembali ke pesanan", color = hijau)
            }
        }
    }
}

@Composable
internal fun BagianBuktiTransfer(
    uri: Uri?,
    onGambarBerubah: (Uri?) -> Unit
) {
    val resolver = LocalContext.current.contentResolver
    var gambar by remember(uri) { mutableStateOf<Bitmap?>(null) }
    var gagalMemuat by remember(uri) { mutableStateOf(false) }

    val pilihGambar = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { hasil ->
        // Jika pemilih gambar dibatalkan, pilihan sebelumnya tetap ada.
        if (hasil != null) onGambarBerubah(hasil)
    }

    LaunchedEffect(uri) {
        if (uri != null) {
            gambar = withContext(Dispatchers.IO) {
                try {
                    bacaPratinjauTransfer(resolver, uri)
                } catch (_: IOException) {
                    null
                } catch (_: SecurityException) {
                    null
                }
            }
            gagalMemuat = gambar == null
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Bukti transfer", fontWeight = FontWeight.SemiBold)

        val bitmap = gambar
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Pratinjau bukti transfer",
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentScale = ContentScale.Fit
            )
        } else if (uri != null) {
            Text(
                text = if (gagalMemuat) {
                    "Pratinjau tidak tersedia. Kamu bisa memilih gambar lain."
                } else {
                    "Memuat gambar…"
                },
                fontSize = 13.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { pilihGambar.launch("image/*") }) {
                Text(if (uri == null) "Pilih gambar" else "Ganti gambar")
            }

            if (uri != null) {
                TextButton(onClick = { onGambarBerubah(null) }) {
                    Text("Hapus")
                }
            }
        }
    }
}

// Baca versi kecil untuk pratinjau agar foto beresolusi besar
// tidak langsung dimuat dengan ukuran penuh ke memori.
private fun bacaPratinjauTransfer(
    resolver: ContentResolver,
    uri: Uri
): Bitmap? {
    val ukuran = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    resolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, ukuran)
    }

    if (ukuran.outWidth <= 0 || ukuran.outHeight <= 0) return null

    var sampel = 1
    val sisiTerpanjang = maxOf(ukuran.outWidth, ukuran.outHeight)
    while (sisiTerpanjang / sampel > 1200) {
        sampel *= 2
    }

    val opsi = BitmapFactory.Options().apply { inSampleSize = sampel }
    return resolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, opsi)
    }
}

@Composable
private fun PilihanInvoice(
    teks: String,
    dipilih: Boolean,
    onKlik: () -> Unit
) {
    FilterChip(
        selected = dipilih,
        onClick = onKlik,
        label = { Text(teks) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFDDEFE4),
            selectedLabelColor = Color(0xFF125139)
        )
    )
}

@Preview(
    name = "Checkout · Transfer Bank",
    showBackground = true,
    backgroundColor = 0xFFE9EEE9,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun CheckoutInvoicePreview() {
    val beras = ProdukPenjualanUi(
        kode = "PR-25H",
        nama = "Beras Premium",
        kemasan = "25 kg hijau",
        satuanJual = "karung",
        stokTersedia = 12,
        hargaKatalog = 350_000L
    )

    SIRABASTheme {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            IsiCheckoutInvoice(
                namaPt = "PT Sri Tani Berkah",
                alamatPt = "",
                teleponPt = "",
                tanggalPesanan = "11 November 2026",
                keranjang = listOf(
                    ItemPesananUi(beras, jumlah = 2, hargaSatuan = 350_000L)
                ),
                daftarPelanggan = listOf("MBG A", "MBG B"),
                onTutup = {},
                onKonfirmasi = { _, _, _, _, _ -> },
                metodeAwal = "Transfer Bank"
            )
        }
    }
}
