package com.stb.sirabas.ui.screens

import android.content.Context
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
import com.stb.sirabas.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.math.BigInteger
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

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
    ) { tujuan ->
        if (tujuan != null) {
            scope.launch {
                val pesan = try {
                    withContext(Dispatchers.IO) {
                        tulisGambarInvoice(
                            context = context,
                            tujuan = tujuan,
                            invoice = invoice,
                            namaPt = namaPt,
                            alamatPt = alamatPt,
                            teleponPt = teleponPt
                        )
                    }
                    "Gambar invoice berhasil disimpan."
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("SIRABAS", "Gagal menyimpan PNG invoice", e)
                    "Gambar invoice gagal disimpan. Coba pilih lokasi lain."
                }

                Toast.makeText(context, pesan, Toast.LENGTH_LONG).show()
            }
        }
    }

    return {
        val namaFile = invoice.nomor.replace(Regex("[^A-Za-z0-9_-]"), "-")
        pilihLokasi.launch("$namaFile.png")
    }
}

private fun tulisGambarInvoice(
    context: Context,
    tujuan: Uri,
    invoice: InvoicePenjualanUi,
    namaPt: String,
    alamatPt: String,
    teleponPt: String
) {
    val formatAngka = NumberFormat.getIntegerInstance(
        Locale.forLanguageTag("id-ID")
    )

    fun rupiah(nilai: BigInteger): String = "Rp${formatAngka.format(nilai)}"

    val hijau = Color.rgb(23, 99, 68)
    val tinta = Color.rgb(32, 48, 40)
    val abu = Color.rgb(83, 99, 90)

    val lebarGambar = 1000
    val margin = 60
    val lebarTeks = lebarGambar - 2 * margin

    val logo = requireNotNull(context.getDrawable(R.drawable.logo_stb)) {
        "Logo Sri Tani tidak ditemukan."
    }.mutate()

    // Pertahankan proporsi asli logo.
    val lebarAsli = logo.intrinsicWidth.coerceAtLeast(1)
    val tinggiAsli = logo.intrinsicHeight.coerceAtLeast(1)
    val skala = 120f / maxOf(lebarAsli, tinggiAsli)
    val lebarLogo = (lebarAsli * skala).roundToInt().coerceAtLeast(1)
    val tinggiLogo = (tinggiAsli * skala).roundToInt().coerceAtLeast(1)

    // Ukur semua teks sebelum membuat PNG agar baris terakhir tidak terpotong.
    val daftarBaris = mutableListOf<Pair<StaticLayout, Int>>()

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

        val layout = StaticLayout.Builder
            .obtain(teks, 0, teks.length, pena, lebarTeks)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .build()

        daftarBaris.add(layout to jarakBawah)
    }

    tambahTeks(namaPt, ukuran = 40f, warna = hijau, tebal = true)

    if (alamatPt.isNotBlank()) {
        tambahTeks(alamatPt, ukuran = 25f, warna = abu)
    }
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
            teks = "${produk.nama} · ${produk.kemasan}",
            tebal = true,
            jarakBawah = 6
        )
        tambahTeks(
            teks = "${item.jumlah} ${produk.satuanJual} × " +
                    "${rupiah(harga)} = ${rupiah(subtotal)}",
            ukuran = 27f,
            warna = abu,
            jarakBawah = 22
        )
    }

    tambahTeks(
        teks = "Total invoice: ${rupiah(invoice.total)}",
        ukuran = 36f,
        warna = hijau,
        tebal = true
    )
    tambahTeks("Sudah dibayar: ${rupiah(invoice.jumlahDibayar.toBigInteger())}")
    tambahTeks("Status: ${if (invoice.lunas) "Lunas" else "Piutang"}")

    if (invoice.jumlahDibayar > 0L && !invoice.metodePembayaran.isNullOrBlank()) {
        tambahTeks("Metode pembayaran terakhir: ${invoice.metodePembayaran}")
    }

    if (!invoice.lunas) {
        tambahTeks("Sisa piutang: ${rupiah(invoice.sisaPiutang)}", tebal = true)
    }

    val awalTeks = margin + tinggiLogo + 16
    val tinggiGambar = awalTeks + margin + daftarBaris.sumOf { (layout, jarak) ->
        layout.height + jarak
    }

    val bitmap = Bitmap.createBitmap(
        lebarGambar,
        tinggiGambar,
        Bitmap.Config.ARGB_8888
    )

    try {
        val kanvas = Canvas(bitmap)
        kanvas.drawColor(Color.WHITE)

        logo.setBounds(margin, margin, margin + lebarLogo, margin + tinggiLogo)
        logo.draw(kanvas)

        var posisiY = awalTeks
        for ((layout, jarak) in daftarBaris) {
            kanvas.save()
            kanvas.translate(margin.toFloat(), posisiY.toFloat())
            layout.draw(kanvas)
            kanvas.restore()
            posisiY += layout.height + jarak
        }

        val output = context.contentResolver.openOutputStream(tujuan, "wt")
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