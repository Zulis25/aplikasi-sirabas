package com.stb.sirabas.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.TextUtils
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.stb.sirabas.R
import java.math.BigInteger
import java.text.NumberFormat
import java.util.Locale

@Composable
fun rememberCetakInvoice(
    invoice: InvoicePenjualanUi,
    namaPt: String,
    alamatPt: String,
    teleponPt: String,
    onGagal: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val activity = remember(context) { cariActivityCetak(context) }

    var dokumenMenunggu by remember {
        mutableStateOf<WebView?>(null)
    }

    DisposableEffect(Unit) {
        onDispose {
            dokumenMenunggu?.destroy()
            dokumenMenunggu = null
        }
    }

    return mulaiCetak@{
        if (dokumenMenunggu != null) return@mulaiCetak

        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            onGagal("Halaman invoice belum siap untuk mencetak.")
            return@mulaiCetak
        }

        val layanan =
            activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager

        if (layanan == null) {
            onGagal("Layanan cetak tidak tersedia di perangkat ini.")
            return@mulaiCetak
        }

        val namaDokumen = "Invoice ${invoice.nomor}"

        try {
            val html = buatHtmlInvoice(
                context = context,
                invoice = invoice,
                namaPt = namaPt,
                alamatPt = alamatPt,
                teleponPt = teleponPt
            )

            val dokumen = WebView(activity)
            dokumenMenunggu = dokumen

            dokumen.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    if (dokumenMenunggu !== view) return

                    try {
                        val adapter = view.createPrintDocumentAdapter(namaDokumen)
                        val atribut = PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .build()

                        layanan.print(namaDokumen, adapter, atribut)
                    } catch (e: RuntimeException) {
                        view.destroy()
                        onGagal("Dialog cetak tidak dapat dibuka.")
                    } finally {
                        dokumenMenunggu = null
                    }
                }
            }

            dokumen.loadDataWithBaseURL(
                null,
                html,
                "text/html",
                "UTF-8",
                null
            )
        } catch (e: Exception) {
            dokumenMenunggu?.destroy()
            dokumenMenunggu = null
            onGagal("Dokumen invoice gagal disiapkan.")
        }
    }
}

private tailrec fun cariActivityCetak(context: Context): Activity? {
    return when (context) {
        is Activity -> context
        is ContextWrapper -> cariActivityCetak(context.baseContext)
        else -> null
    }
}

private fun buatHtmlInvoice(
    context: Context,
    invoice: InvoicePenjualanUi,
    namaPt: String,
    alamatPt: String,
    teleponPt: String
): String {
    val formatAngka = NumberFormat.getIntegerInstance(
        Locale.forLanguageTag("id-ID")
    )

    fun rupiah(nilai: BigInteger): String =
        "Rp${formatAngka.format(nilai)}"

    fun teks(nilai: String): String =
        TextUtils.htmlEncode(nilai)

    val logoBase64 = context.resources
        .openRawResource(R.drawable.logo_stb)
        .use { input ->
            Base64.encodeToString(input.readBytes(), Base64.NO_WRAP)
        }

    val alamat = if (alamatPt.isBlank()) {
        ""
    } else {
        "<div>${teks(alamatPt)}</div>"
    }

    val telepon = if (teleponPt.isBlank()) {
        ""
    } else {
        "<div>Telp. ${teks(teleponPt)}</div>"
    }

    val barisProduk = invoice.daftarItem.joinToString("\n") { item ->
        val produk = item.produk
        val hargaSatuan = item.hargaSatuan.toBigInteger()
        val subtotal = hargaSatuan * item.jumlah.toBigInteger()

        """
        <tr>
            <td>
                <strong>${teks(produk.nama)}</strong><br>
                <small>${teks(produk.kemasan)}</small>
            </td>
            <td>${item.jumlah} ${teks(produk.satuanJual)}</td>
            <td class="angka">${rupiah(hargaSatuan)}</td>
            <td class="angka">${rupiah(subtotal)}</td>
        </tr>
        """.trimIndent()
    }

    val status = if (invoice.lunas) "Lunas" else "Piutang"
    val metode = invoice.metodePembayaran

    val barisMetode = if (invoice.jumlahDibayar > 0L && !metode.isNullOrBlank()) {
        "<p>Metode pembayaran terakhir: ${teks(metode)}</p>"
    } else {
        ""
    }

    val barisPiutang = if (invoice.lunas) {
        ""
    } else {
        "<p><strong>Sisa piutang: ${rupiah(invoice.sisaPiutang)}</strong></p>"
    }

    return """
        <!doctype html>
        <html lang="id">
        <head>
            <meta charset="UTF-8">
            <title>Invoice ${teks(invoice.nomor)}</title>
            <style>
                body {
                    font-family: sans-serif;
                    color: #203028;
                    padding: 18px;
                }
                .perusahaan {
                    display: flex;
                    align-items: center;
                    margin-bottom: 12px;
                }
                .logo {
                    width: 64px;
                    height: 64px;
                    object-fit: contain;
                    margin-right: 12px;
                }
                h1 { font-size: 24px; margin: 24px 0 4px; }
                h2 { font-size: 20px; color: #176344; margin: 0 0 5px; }
                p { margin: 6px 0; }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 22px 0;
                }
                th, td {
                    padding: 9px 5px;
                    border-bottom: 1px solid #ccd8ce;
                    text-align: left;
                    vertical-align: top;
                }
                th { color: #123f2e; }
                small { color: #506657; }
                .angka { text-align: right; }
                tr, .ringkasan { page-break-inside: avoid; }
            </style>
        </head>
        <body>
            <div class="perusahaan">
                <img class="logo" src="data:image/png;base64,$logoBase64" alt="">
                <div>
                    <h2>${teks(namaPt)}</h2>
                    $alamat
                    $telepon
                </div>
            </div>

            <h1>Invoice</h1>
            <p>${teks(invoice.nomor)}</p>
            <p>Tanggal pesanan: ${teks(invoice.tanggal)}</p>
            <p>Pelanggan: ${teks(invoice.pelanggan)}</p>

            <table>
                <thead>
                    <tr>
                        <th>Produk</th>
                        <th>Jumlah</th>
                        <th class="angka">Harga satuan</th>
                        <th class="angka">Subtotal</th>
                    </tr>
                </thead>
                <tbody>
                    $barisProduk
                </tbody>
            </table>

            <div class="ringkasan">
                <h2>Total invoice: ${rupiah(invoice.total)}</h2>
                <p>Sudah dibayar: ${rupiah(invoice.jumlahDibayar.toBigInteger())}</p>
                <p>Status pembayaran: $status</p>
                $barisMetode
                $barisPiutang
            </div>
        </body>
        </html>
    """.trimIndent()
}