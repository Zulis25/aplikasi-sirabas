package com.stb.sirabas.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.TextUtils
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream

@Composable
fun QrDanCetakLabelKarung(
    kodeKarung: String,
    kodeBatch: String,
    kodeLot: String,
    tanggal: String,
    namaProduk: String,
    kemasan: String
) {
    val context = LocalContext.current
    val activity = remember(context) { activityUntukLabel(context) }
    val qr = remember(kodeKarung) { buatQrKarung(kodeKarung) }
    var dokumenMenunggu by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            dokumenMenunggu?.destroy()
            dokumenMenunggu = null
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = qr.asImageBitmap(),
            contentDescription = "QR karung $kodeKarung",
            modifier = Modifier.size(152.dp)
        )

        Button(
            enabled = dokumenMenunggu == null,
            onClick = cetak@{
                if (dokumenMenunggu != null) return@cetak

                if (activity == null || activity.isFinishing || activity.isDestroyed) {
                    Toast.makeText(context, "Halaman cetak belum siap.", Toast.LENGTH_SHORT).show()
                    return@cetak
                }

                val layanan = activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                if (layanan == null) {
                    Toast.makeText(context, "Layanan cetak tidak tersedia.", Toast.LENGTH_LONG).show()
                    return@cetak
                }

                try {
                    val html = htmlLabelKarung(
                        qr, kodeKarung, kodeBatch, kodeLot, tanggal, namaProduk, kemasan
                    )
                    val dokumen = WebView(activity)
                    dokumenMenunggu = dokumen

                    dokumen.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String?) {
                            if (dokumenMenunggu !== view) return

                            try {
                                val namaDokumen = "Label ${kodeKarung.replace('/', '-')}"
                                val adapter = view.createPrintDocumentAdapter(namaDokumen)
                                layanan.print(
                                    namaDokumen,
                                    adapter,
                                    PrintAttributes.Builder().build()
                                )
                            } catch (e: RuntimeException) {
                                view.destroy()
                                Toast.makeText(
                                    context, "Dialog cetak gagal dibuka.", Toast.LENGTH_LONG
                                ).show()
                            } finally {
                                dokumenMenunggu = null
                            }
                        }
                    }

                    dokumen.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                } catch (e: Exception) {
                    dokumenMenunggu?.destroy()
                    dokumenMenunggu = null
                    Toast.makeText(context, "Label gagal disiapkan.", Toast.LENGTH_LONG).show()
                }
            }
        ) {
            Text(if (dokumenMenunggu == null) "Cetak label / PDF" else "Menyiapkan label…")
        }
    }
}

private fun buatQrKarung(kode: String): Bitmap {
    val ukuran = 320
    val matriks = QRCodeWriter().encode(kode, BarcodeFormat.QR_CODE, ukuran, ukuran)
    val piksel = IntArray(ukuran * ukuran) { indeks ->
        val x = indeks % ukuran
        val y = indeks / ukuran
        if (matriks[x, y]) Color.BLACK else Color.WHITE
    }

    return Bitmap.createBitmap(piksel, ukuran, ukuran, Bitmap.Config.ARGB_8888)
}

private tailrec fun activityUntukLabel(context: Context): Activity? =
    when (context) {
        is Activity -> context
        is ContextWrapper -> activityUntukLabel(context.baseContext)
        else -> null
    }

private fun htmlLabelKarung(
    qr: Bitmap,
    kode: String,
    batch: String,
    lot: String,
    tanggal: String,
    produk: String,
    kemasan: String
): String {
    val gambar = ByteArrayOutputStream().use { output ->
        check(qr.compress(Bitmap.CompressFormat.PNG, 100, output))
        Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    fun teks(nilai: String): String = TextUtils.htmlEncode(nilai)

    return """
        <!doctype html>
        <html lang="id">
        <head>
            <meta charset="UTF-8">
            <title>Label ${teks(kode)}</title>
            <style>
                body { font-family: sans-serif; color: black; margin: 0; }
                .label {
                    box-sizing: border-box; width: 75mm; max-width: 100%;
                    padding: 3mm; border: 1px solid black;
                    page-break-inside: avoid; overflow-wrap: anywhere;
                }
                h2 { font-size: 13pt; margin: 0 0 2mm; }
                p { font-size: 10pt; margin: 1mm 0; }
                .qr { text-align: center; margin-top: 2mm; }
                img { width: 30mm; height: 30mm; }
            </style>
        </head>
        <body>
            <div class="label">
                <h2>PT. Sri Tani Berkah</h2>
                <p><strong>${teks(produk)} · ${teks(kemasan)}</strong></p>
                <p>Batch: ${teks(batch)}</p>
                <p>Lot bahan: ${teks(lot)}</p>
                <p>Tanggal produksi: ${teks(tanggal)}</p>
                <div class="qr">
                    <img src="data:image/png;base64,$gambar" alt="QR karung">
                    <p><strong>${teks(kode)}</strong></p>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()
}