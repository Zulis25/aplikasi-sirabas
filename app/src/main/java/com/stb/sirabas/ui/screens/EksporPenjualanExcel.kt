package com.stb.sirabas.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private const val MIME_EXCEL =
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

private data class LaporanExcelPenjualan(
    val periode: String,
    val invoice: List<InvoicePenjualanUi>
)

@Composable
fun LaporanPenjualanDenganExcel(
    daftarInvoice: List<InvoicePenjualanUi>,
    onPesan: (String) -> Unit
) {
    val resolver = LocalContext.current.contentResolver
    val scope = rememberCoroutineScope()
    var laporanDipilih by remember {
        mutableStateOf<LaporanExcelPenjualan?>(null)
    }

    val pilihLokasi = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(MIME_EXCEL)
    ) { uri ->
        val laporan = laporanDipilih
        laporanDipilih = null

        if (uri != null && laporan == null) {
            onPesan("Silakan pilih periode dan unduh laporan kembali.")
        } else if (uri != null && laporan != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val output = resolver.openOutputStream(uri, "wt")
                            ?: error("Lokasi penyimpanan tidak dapat dibuka.")

                        output.use { tulisExcelPenjualan(it, laporan) }
                    }
                    onPesan("Laporan penjualan berhasil disimpan.")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    onPesan(
                        "Laporan gagal disimpan. " +
                                (e.localizedMessage ?: "Coba pilih lokasi lain.")
                    )
                }
            }
        }
    }

    LaporanPenjualanScreen(
        daftarInvoice = daftarInvoice,
        onUnduhExcel = { mulai, akhir, invoiceTerpilih ->
            val formatNama = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
            val formatTampil = SimpleDateFormat(
                "d MMMM yyyy",
                Locale.forLanguageTag("id-ID")
            )

            laporanDipilih = LaporanExcelPenjualan(
                periode = "${formatTampil.format(mulai)} – ${formatTampil.format(akhir)}",
                invoice = invoiceTerpilih.toList()
            )

            val namaFile = "Penjualan_SIRABAS_" +
                    "${formatNama.format(mulai)}_${formatNama.format(akhir)}.xlsx"

            pilihLokasi.launch(namaFile)
        }
    )
}

internal fun tulisLembarSirabas(
    output: OutputStream,
    namaLembar: String,
    isiLembar: ((List<Any>) -> Unit) -> Unit
) {
    val nsLembar = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
    val nsRelasi = "http://schemas.openxmlformats.org/package/2006/relationships"
    val relDokumen =
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
    val tipe = "application/vnd.openxmlformats-officedocument"

    ZipOutputStream(output.buffered()).use { zip ->
        fun bagian(nama: String, isi: String) {
            zip.putNextEntry(ZipEntry(nama))
            zip.write(isi.trimIndent().toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }

        bagian(
            "[Content_Types].xml",
            """
            <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
              <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
              <Default Extension="xml" ContentType="application/xml"/>
              <Override PartName="/xl/workbook.xml" ContentType="$tipe.spreadsheetml.sheet.main+xml"/>
              <Override PartName="/xl/worksheets/sheet1.xml" ContentType="$tipe.spreadsheetml.worksheet+xml"/>
            </Types>
            """
        )

        bagian(
            "_rels/.rels",
            """
            <Relationships xmlns="$nsRelasi">
              <Relationship Id="rId1" Type="$relDokumen/officeDocument" Target="xl/workbook.xml"/>
            </Relationships>
            """
        )

        bagian(
            "xl/workbook.xml",
            """
            <workbook xmlns="$nsLembar" xmlns:r="$relDokumen">
              <sheets><sheet name="${'$'}namaLembar" sheetId="1" r:id="rId1"/></sheets>
            </workbook>
            """
        )

        bagian(
            "xl/_rels/workbook.xml.rels",
            """
            <Relationships xmlns="$nsRelasi">
              <Relationship Id="rId1" Type="$relDokumen/worksheet" Target="worksheets/sheet1.xml"/>
            </Relationships>
            """
        )

        zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
        val penulis = zip.bufferedWriter(Charsets.UTF_8)

        fun teksXml(teks: String): String = teks
            .filter { it >= ' ' || it == '\n' || it == '\r' || it == '\t' }
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

        var nomorBaris = 0

        fun baris(vararg nilai: Any) {
            nomorBaris++
            penulis.write("""<row r="$nomorBaris">""")

            nilai.forEachIndexed { indeks, isi ->
                val alamat = "${'A' + indeks}$nomorBaris"
                val teks = isi.toString()

                // Excel menyimpan maksimal 15 digit secara presisi.
                val angka = when (isi) {
                    is java.math.BigDecimal -> isi.stripTrailingZeros().precision() <= 15
                    is BigInteger, is Long, is Int ->
                        teks.removePrefix("-").length <= 15
                    else -> false
                }
                if (angka) {
                    penulis.write("""<c r="$alamat"><v>$teks</v></c>""")
                } else {
                    penulis.write(
                        """<c r="$alamat" t="inlineStr"><is><t xml:space="preserve">"""
                    )
                    penulis.write(teksXml(teks))
                    penulis.write("</t></is></c>")
                }
            }

            penulis.write("</row>")
        }

        penulis.write("""<worksheet xmlns="$nsLembar">""")
        penulis.write(
            """
            <cols>
              <col min="1" max="1" width="32" customWidth="1"/>
              <col min="2" max="3" width="26" customWidth="1"/>
              <col min="4" max="7" width="21" customWidth="1"/>
            </cols>
            <sheetData>
            """.trimIndent()
        )

        isiLembar { kolom ->
            baris(*kolom.toTypedArray())
        }
        penulis.write("</sheetData></worksheet>")
        penulis.flush()
        zip.closeEntry()
    }
}
private fun tulisExcelPenjualan(
    output: OutputStream,
    laporan: LaporanExcelPenjualan
) {
    tulisLembarSirabas(output, "Penjualan") { baris ->
        baris(listOf("PT. Sri Tani Berkah"))
        baris(listOf("Laporan penjualan"))
        baris(listOf("Periode", laporan.periode))
        baris(
            listOf(
                "Nomor invoice", "Tanggal pesanan", "Pelanggan",
                "Total invoice (Rp)", "Sudah dibayar (Rp)",
                "Sisa piutang (Rp)", "Status"
            )
        )

        var totalPenjualan = BigInteger.ZERO
        var totalDibayar = BigInteger.ZERO
        var totalPiutang = BigInteger.ZERO

        laporan.invoice.forEach { invoice ->
            baris(
                listOf(
                    invoice.nomor,
                    invoice.tanggal,
                    invoice.pelanggan,
                    invoice.total,
                    invoice.jumlahDibayar,
                    invoice.sisaPiutang,
                    if (invoice.lunas) "Lunas" else "Piutang"
                )
            )

            totalPenjualan += invoice.total
            totalDibayar += invoice.jumlahDibayar.toBigInteger()
            totalPiutang += invoice.sisaPiutang
        }

        baris(listOf("TOTAL", "", "", totalPenjualan, totalDibayar, totalPiutang))
    }
}