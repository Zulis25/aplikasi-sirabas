package com.stb.sirabas.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// Menyatukan data invoice dan lampiran yang dipilih saat Checkout.
private data class HasilCheckoutUi(
    val invoice: InvoicePenjualanUi,
    val buktiTransfer: Uri?
)

@Composable
fun AlurPenjualanScreen(
    namaPetugas: String,
    namaPt: String,
    alamatPt: String,
    teleponPt: String,
    trenHarian: List<PenjualanHarianUi>,
    hasilProduksi: List<ProdukPenjualanUi>,
    hasilSamping: List<ProdukPenjualanUi>,
    daftarPelanggan: List<String>,
    onKonfirmasiInvoice: (
        tanggal: String,
        pelanggan: String,
        itemFinal: List<ItemPesananUi>,
        status: String,
        metode: String?
    ) -> Unit
) {
    val keranjang = remember { mutableStateListOf<ItemPesananUi>() }
    var tanggalCheckout by remember { mutableStateOf<String?>(null) }
    var hasilCheckout by remember { mutableStateOf<HasilCheckoutUi?>(null) }

    val hasil = hasilCheckout

    BackHandler(enabled = hasil != null) {
        hasilCheckout = null
    }

    if (hasil != null) {
        DetailSetelahCheckout(
            hasil = hasil,
            namaPt = namaPt,
            alamatPt = alamatPt,
            teleponPt = teleponPt,
            onKembali = { hasilCheckout = null }
        )
        return
    }

    BerandaPenjualanScreen(
        namaPetugas = namaPetugas,
        trenHarian = trenHarian,
        hasilProduksi = hasilProduksi,
        hasilSamping = hasilSamping,
        keranjang = keranjang.toList(),

        onTambah = { produk, jumlah, harga ->
            val sudahDipesan = keranjang
                .filter { it.produk.kode == produk.kode }
                .sumOf { it.jumlah }

            val sisa = produk.stokTersedia - sudahDipesan

            if (jumlah in 1..sisa && harga > 0L) {
                val indeks = keranjang.indexOfFirst {
                    it.produk.kode == produk.kode && it.hargaSatuan == harga
                }

                if (indeks >= 0) {
                    val itemLama = keranjang[indeks]
                    keranjang[indeks] = itemLama.copy(
                        jumlah = itemLama.jumlah + jumlah
                    )
                } else {
                    keranjang.add(ItemPesananUi(produk, jumlah, harga))
                }
            }
        },

        onCheckout = {
            if (keranjang.isNotEmpty()) {
                val formatTanggal = SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale.forLanguageTag("id-ID")
                )
                tanggalCheckout = formatTanggal.format(Date())
            }
        }
    )

    tanggalCheckout?.let { tanggal ->
        CheckoutInvoiceDialogDenganBukti(
            namaPt = namaPt,
            alamatPt = alamatPt,
            teleponPt = teleponPt,
            tanggalPesanan = tanggal,
            keranjang = keranjang.toList(),
            daftarPelanggan = daftarPelanggan,
            onTutup = { tanggalCheckout = null },

            onKonfirmasi = { pelanggan, itemFinal, status, metode, bukti ->
                val kodeLokal = UUID.randomUUID()
                    .toString()
                    .take(8)
                    .uppercase(Locale.ROOT)

                // Gunakan harga akhir dari Checkout, termasuk perubahan harga.
                val invoiceAwal = InvoicePenjualanUi(
                    nomor = "INV-$kodeLokal",
                    tanggal = tanggal,
                    pelanggan = pelanggan,
                    daftarItem = itemFinal.toList(),
                    jumlahDibayar = 0L,
                    metodePembayaran = metode
                )

                val invoiceFinal = if (status == "Lunas") {
                    invoiceAwal.copy(jumlahDibayar = invoiceAwal.total.toLong())
                } else {
                    invoiceAwal
                }

                hasilCheckout = HasilCheckoutUi(
                    invoice = invoiceFinal,
                    buktiTransfer = bukti
                )

                tanggalCheckout = null
                keranjang.clear()

                onKonfirmasiInvoice(
                    tanggal, pelanggan, itemFinal, status, metode
                )
            }
        )
    }
}

@Composable
fun AlurPenjualanScreen(
    namaPetugas: String,
    namaPt: String,
    alamatPt: String,
    teleponPt: String,
    trenHarian: List<PenjualanHarianUi>,
    hasilProduksi: List<ProdukPenjualanUi>,
    hasilSamping: List<ProdukPenjualanUi>,
    daftarPelanggan: List<String>,
    onKonfirmasiInvoice: (InvoicePenjualanUi) -> Unit
) {
    val keranjang = remember { mutableStateListOf<ItemPesananUi>() }

    var tanggalCheckout by remember { mutableStateOf("") }
    var nomorCheckout by remember { mutableStateOf<String?>(null) }
    var hasilCheckout by remember { mutableStateOf<HasilCheckoutUi?>(null) }

    val hasil = hasilCheckout

    BackHandler(enabled = hasil != null) {
        hasilCheckout = null
    }

    if (hasil != null) {
        DetailSetelahCheckout(
            hasil = hasil,
            namaPt = namaPt,
            alamatPt = alamatPt,
            teleponPt = teleponPt,
            onKembali = { hasilCheckout = null }
        )
        return
    }

    BerandaPenjualanScreen(
        namaPetugas = namaPetugas,
        trenHarian = trenHarian,
        hasilProduksi = hasilProduksi,
        hasilSamping = hasilSamping,
        keranjang = keranjang.toList(),

        onTambah = { produk, jumlah, harga ->
            val sudahDipesan = keranjang
                .filter { it.produk.kode == produk.kode }
                .sumOf { it.jumlah }

            val sisa = produk.stokTersedia - sudahDipesan

            if (jumlah in 1..sisa && harga > 0L) {
                val indeks = keranjang.indexOfFirst { item ->
                    item.produk.kode == produk.kode &&
                            item.hargaSatuan == harga
                }

                if (indeks >= 0) {
                    val itemLama = keranjang[indeks]
                    keranjang[indeks] = itemLama.copy(
                        jumlah = itemLama.jumlah + jumlah
                    )
                } else {
                    keranjang.add(ItemPesananUi(produk, jumlah, harga))
                }
            }
        },

        onCheckout = {
            if (keranjang.isNotEmpty() && nomorCheckout == null) {
                tanggalCheckout = SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale.forLanguageTag("id-ID")
                ).format(Date())

                val kode = UUID.randomUUID()
                    .toString()
                    .take(8)
                    .uppercase(Locale.ROOT)

                nomorCheckout = "INV-$kode"
            }
        }
    )

    nomorCheckout?.let { nomor ->
        CheckoutPenjualanDialog(
            nomorInvoice = nomor,
            namaPt = namaPt,
            alamatPt = alamatPt,
            teleponPt = teleponPt,
            tanggalPesanan = tanggalCheckout,
            keranjang = keranjang.toList(),
            daftarPelanggan = daftarPelanggan,
            onTutup = { nomorCheckout = null },

            onInvoiceDibuat = { invoice ->
                // Invoice yang sama dipakai di detail dan dikirim ke riwayat.
                onKonfirmasiInvoice(invoice)

                hasilCheckout = HasilCheckoutUi(
                    invoice = invoice,
                    buktiTransfer = invoice.riwayatPembayaran
                        .lastOrNull()
                        ?.buktiTransfer
                )

                nomorCheckout = null
                keranjang.clear()
            }
        )
    }
}
@Composable
private fun DetailSetelahCheckout(
    hasil: HasilCheckoutUi,
    namaPt: String,
    alamatPt: String,
    teleponPt: String,
    onKembali: () -> Unit
) {
    val context = LocalContext.current
    var pesan by remember { mutableStateOf<String?>(null) }
    val invoice = hasil.invoice
    val bukti = hasil.buktiTransfer
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
        onGagal = { keterangan -> pesan = keterangan }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onKembali) {
                Text("‹  Beranda", color = AccessColors.Green)
            }

            if (bukti != null) {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(bukti, "image/*")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        try {
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                            pesan = "Tidak ada aplikasi pembuka gambar di perangkat ini."
                        } catch (_: SecurityException) {
                            pesan = "Bukti transfer tidak dapat diakses."
                        }
                    }
                ) {
                    Text("Lihat bukti transfer", color = AccessColors.Green)
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
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
                onUnduhGambar = simpanGambar,
                onCetak = cetakInvoice
            )
        }
    }

    pesan?.let { isi ->
        AlertDialog(
            onDismissRequest = { pesan = null },
            text = { Text(isi) },
            confirmButton = {
                TextButton(onClick = { pesan = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}