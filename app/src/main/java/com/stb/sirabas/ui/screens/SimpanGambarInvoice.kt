package com.stb.sirabas.ui.screens

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.math.BigInteger
import java.text.NumberFormat
import java.util.Locale

@Composable
fun rememberSimpanGambarInvoice(
    invoice: InvoicePenjualanUi,
    namaPt: String,
    alamatPt: String,
    teleponPt: String
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pilihLokasi = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { tujuan: Uri? ->
        if (tujuan != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        tulisGambarInvoice(
                            resolver = context.contentResolver,
                            tujuan = tujuan,
                            invoice = invoice,
                            namaPt = namaPt,
                            alamatPt = alamatPt,
                            teleponPt = teleponPt
                        )
                    }
                    Toast.makeText(
                        context,
                        "Gambar invoice tersimpan.",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: IOException) {
                    Log.e("SIRABAS", "Gagal menulis gambar invoice", e)
                    Toast.makeText(
                        context,
                        "Gagal menyimpan invoice. Coba lokasi lain.",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: SecurityException) {
                    Log.e("SIRABAS", "Lokasi penyimpanan tidak dapat diakses", e)
                    Toast.makeText(
                        context,
                        "Lokasi penyimpanan tidak dapat diakses.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    return {
        // Contoh: INV/2026/001 menjadi INV-2026-001.png.
        val namaFile = invoice.nomor.replace(Regex("[^A-Za-z0-9_-]"), "-")
        pilihLokasi.launch("$namaFile.png")
    }
}

private fun tulisGambarInvoice(
    resolver: ContentResolver,
    tujuan: Uri,
    invoice: InvoicePenjualanUi,
    namaPt: String,
    alamatPt: String,
    teleponPt: String
) {
    val formatAngka = NumberFormat.getIntegerInstance(
        Locale.forLanguageTag("id-ID")
    )
    fun rupiah(nilai: BigInteger) = "Rp${formatAngka.format(nilai)}"

    val hijau = Color.rgb(23, 99, 68)
    val tinta = Color.rgb(32, 48, 40)
    val abu = Color.rgb(83, 99, 90)
    val lebar = 1000
    val margin = 60
    val lebarIsi = lebar - (margin * 2)

    // Ukur teks lebih dahulu supaya alamat panjang, nama pelanggan,
    // dan semua produk tetap masuk ke gambar.
    val baris = mutableListOf<Pair<StaticLayout, Int>>()

    fun tambahTeks(
        teks: String,
        ukuran: Float = 30f,
        warna: Int = tinta,
        tebal: Boolean = false,
        jarakBawah: Int = 12
    ) {
        val pena = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            color = warna
            textSize = ukuran
            typeface = if (tebal) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

        val tataLetak = StaticLayout.Builder
            .obtain(teks, 0, teks.length, pena, lebarIsi)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .build()

        baris.add(tataLetak to jarakBawah)
    }

    tambahTeks(namaPt, ukuran = 40f, warna = hijau, tebal = true)
    if (alamatPt.isNotBlank()) tambahTeks(alamatPt, ukuran = 25f, warna = abu)
    if (teleponPt.isNotBlank()) {
        tambahTeks("Telp. $teleponPt", ukuran = 25f, warna = abu)
    }

    tambahTeks("INVOICE", ukuran = 48f, tebal = true)
    tambahTeks(invoice.nomor, warna = hijau, tebal = true)
    tambahTeks("Tanggal pesanan: ${invoice.tanggal}")
    tambahTeks("Pelanggan: ${invoice.pelanggan}", jarakBawah = 32)

    tambahTeks("Rincian pesanan", ukuran = 32f, tebal = true)

    invoice.daftarItem.forEach { item ->
        val produk = item.produk
        val harga = item.hargaSatuan.toBigInteger()
        val subtotal = harga * item.jumlah.toBigInteger()

        tambahTeks(
            "${produk.nama} · ${produk.kemasan}",
            tebal = true,
            jarakBawah = 6
        )
        tambahTeks(
            "${item.jumlah} ${produk.satuanJual} × ${rupiah(harga)} = ${rupiah(subtotal)}",
            ukuran = 27f,
            warna = abu,
            jarakBawah = 22
        )
    }

    tambahTeks(
        "Total invoice: ${rupiah(invoice.total)}",
        ukuran = 36f,
        warna = hijau,
        tebal = true
    )
    tambahTeks("Sudah dibayar: ${rupiah(invoice.jumlahDibayar.toBigInteger())}")
    tambahTeks("Status: ${if (invoice.lunas) "Lunas" else "Piutang"}")

    if (invoice.jumlahDibayar > 0 && !invoice.metodePembayaran.isNullOrBlank()) {
        tambahTeks("Metode pembayaran terakhir: ${invoice.metodePembayaran}")
    }

    if (!invoice.lunas) {
        tambahTeks("Sisa piutang: ${rupiah(invoice.sisaPiutang)}", tebal = true)
    }

    val tinggi = margin * 2 + baris.sumOf { (layout, jarak) ->
        layout.height + jarak
    }

    val bitmap = Bitmap.createBitmap(lebar, tinggi, Bitmap.Config.ARGB_8888)

    try {
        val kanvas = Canvas(bitmap)
        kanvas.drawColor(Color.WHITE)
        var posisiY = margin

        for ((layout, jarak) in baris) {
            kanvas.save()
            kanvas.translate(margin.toFloat(), posisiY.toFloat())
            layout.draw(kanvas)
            kanvas.restore()
            posisiY += layout.height + jarak
        }

        val output = resolver.openOutputStream(tujuan, "wt")
            ?: throw IOException("File tujuan tidak dapat dibuka.")

        output.use { stream ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                throw IOException("Gambar PNG gagal ditulis.")
            }
        }
    } finally {
        bitmap.recycle()
    }
}