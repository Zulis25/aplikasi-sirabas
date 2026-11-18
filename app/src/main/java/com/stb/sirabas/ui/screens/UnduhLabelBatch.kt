package com.stb.sirabas.ui.screens

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.math.BigInteger
import kotlin.coroutines.coroutineContext

private data class ProdukLabelPdf(
    val nomorProduk: Int,
    val nama: String,
    val kemasan: String,
    val jumlah: BigInteger
)

private val polaKemasanPdf = Regex(
    """^(\d+(?:[.,]\d+)?)\s*kg\b""",
    RegexOption.IGNORE_CASE
)

private fun produkUntukLabelPdf(batch: BatchProduksiUi): List<ProdukLabelPdf> {
    return batch.rincian.hasilUtama.mapIndexedNotNull { indeks, hasil ->
        val beratKemasan = polaKemasanPdf.find(hasil.kemasan.trim())
            ?.groupValues
            ?.get(1)
            ?.replace(',', '.')
            ?.toBigDecimalOrNull()
            ?: return@mapIndexedNotNull null

        if (beratKemasan.signum() <= 0 || hasil.beratKg.signum() <= 0) {
            return@mapIndexedNotNull null
        }

        val jumlah = hasil.beratKg
            .divideToIntegralValue(beratKemasan)
            .toBigInteger()

        if (jumlah.signum() == 0) return@mapIndexedNotNull null

        ProdukLabelPdf(
            // Pakai indeks asli supaya kodenya sama dengan tampilan label satuan.
            nomorProduk = indeks + 1,
            nama = hasil.namaProduk,
            kemasan = hasil.kemasan,
            jumlah = jumlah
        )
    }
}

@Composable
fun TombolUnduhLabelBatch(batch: BatchProduksiUi) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val produk = produkUntukLabelPdf(batch)
    val totalLabel = produk.fold(BigInteger.ZERO) { total, item ->
        total + item.jumlah
    }

    var sedangMenyimpan by remember(batch.kode) { mutableStateOf(false) }

    val pilihLokasi = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { tujuan ->
        if (tujuan != null) {
            sedangMenyimpan = true

            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver.openOutputStream(tujuan, "wt")
                            ?: error("File tujuan tidak bisa dibuka.")

                        output.use {
                            tulisPdfLabelBatch(it, batch, produk)
                        }
                    }

                    Toast.makeText(
                        context,
                        "$totalLabel label berhasil disimpan dalam satu PDF.",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "PDF gagal disimpan. ${e.localizedMessage.orEmpty()}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    sedangMenyimpan = false
                }
            }
        }
    }

    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = totalLabel.signum() > 0 && !sedangMenyimpan,
        onClick = {
            val kodeFile = batch.kode.replace(Regex("[^A-Za-z0-9_-]"), "-")
            pilihLokasi.launch("Label-$kodeFile.pdf")
        }
    ) {
        Text(
            if (sedangMenyimpan) "Sedang menyimpan label…"
            else "Unduh semua label batch ($totalLabel) · PDF"
        )
    }
}

private suspend fun tulisPdfLabelBatch(
    output: OutputStream,
    batch: BatchProduksiUi,
    daftarProduk: List<ProdukLabelPdf>
) {
    val qrWriter = QRCodeWriter()
    val dokumen = PdfDocument()

    // Ukuran halaman sekitar 75 × 75 mm. Satu halaman untuk satu stiker.
    val ukuranHalaman = 213
    var nomorHalaman = 0

    val pena = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }

    try {
        for (produk in daftarProduk) {
            var nomorKarung = BigInteger.ONE

            while (nomorKarung <= produk.jumlah) {
                coroutineContext.ensureActive()
                nomorHalaman++

                val baris = produk.nomorProduk.toString().padStart(2, '0')
                val urutan = nomorKarung.toString().padStart(3, '0')
                val kode = "${batch.kode}-P$baris-K$urutan"

                val info = PdfDocument.PageInfo.Builder(
                    ukuranHalaman, ukuranHalaman, nomorHalaman
                ).create()

                val halaman = dokumen.startPage(info)
                try {
                    val kanvas = halaman.canvas
                    kanvas.drawColor(Color.WHITE)

                    fun teks(
                        isi: String,
                        posisiY: Float,
                        ukuran: Float,
                        tebal: Boolean = false
                    ) {
                        pena.typeface = if (tebal) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                        pena.textSize = ukuran
                        val lebarTeks = pena.measureText(isi)
                        val lebarTersedia = ukuranHalaman - 20f

                        if (lebarTeks > lebarTersedia) {
                            pena.textSize = ukuran * lebarTersedia / lebarTeks
                        }

                        kanvas.drawText(isi, 10f, posisiY, pena)
                    }

                    teks("PT. Sri Tani Berkah", 18f, 12f, true)
                    teks("${produk.nama} · ${produk.kemasan}", 34f, 11f, true)
                    teks("Batch: ${batch.kode}   Lot: ${batch.kodeBahan}", 49f, 9f)
                    teks("Tanggal produksi: ${batch.tanggal}", 62f, 9f)

                    // QR dan tulisan di bawahnya memakai kode yang sama.
                    val ukuranQr = 256
                    val matriks = qrWriter.encode(
                        kode, BarcodeFormat.QR_CODE, ukuranQr, ukuranQr
                    )
                    val piksel = IntArray(ukuranQr * ukuranQr) { i ->
                        if (matriks[i % ukuranQr, i / ukuranQr]) Color.BLACK
                        else Color.WHITE
                    }
                    val bitmap = Bitmap.createBitmap(
                        piksel, ukuranQr, ukuranQr, Bitmap.Config.ARGB_8888
                    )

                    try {
                        kanvas.drawBitmap(bitmap, null, Rect(49, 66, 164, 181), null)
                    } finally {
                        bitmap.recycle()
                    }

                    teks(kode, 192f, 11f, true)
                    teks("Karung $nomorKarung dari ${produk.jumlah}", 205f, 9f)
                } finally {
                    dokumen.finishPage(halaman)
                }

                nomorKarung += BigInteger.ONE
            }
        }

        coroutineContext.ensureActive()
        dokumen.writeTo(output)
    } finally {
        dokumen.close()
    }
}