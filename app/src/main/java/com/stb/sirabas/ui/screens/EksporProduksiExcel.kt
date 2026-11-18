package com.stb.sirabas.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

private data class DataEksporProduksi(
    val permintaan: PermintaanLaporanProduksiUi,
    val penerimaan: List<BahanBakuUi>,
    val batch: List<BatchProduksiUi>
)

@Composable
fun LaporanProduksiDenganExcel(
    penerimaan: List<BahanBakuUi>,
    batchProduksi: List<BatchProduksiUi>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pilihan by remember { mutableStateOf<DataEksporProduksi?>(null) }

    val pilihLokasi = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    ) { uri ->
        val data = pilihan
        pilihan = null

        if (uri != null && data == null) {
            Toast.makeText(
                context,
                "Pilih kembali periode dan tekan Unduh Excel.",
                Toast.LENGTH_LONG
            ).show()
        } else if (uri != null && data != null) {
            scope.launch {
                val pesan = try {
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver.openOutputStream(uri, "wt")
                            ?: error("Lokasi penyimpanan tidak dapat dibuka.")

                        output.use { tulisLaporanProduksi(it, data) }
                    }
                    "Laporan produksi berhasil disimpan."
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    "Laporan gagal disimpan. ${e.localizedMessage.orEmpty()}"
                }

                Toast.makeText(context, pesan, Toast.LENGTH_LONG).show()
            }
        }
    }

    LaporanProduksiScreen(
        onUnduhExcel = { permintaan ->
            pilihan = DataEksporProduksi(
                permintaan = permintaan,
                penerimaan = penerimaan.toList(),
                batch = batchProduksi.toList()
            )

            val jenis = permintaan.jenis.replace(' ', '_')
            pilihLokasi.launch(
                "SIRABAS_${jenis}_${permintaan.tanggalMulai}_${permintaan.tanggalAkhir}.xlsx"
            )
        }
    )
}

private fun tulisLaporanProduksi(
    output: OutputStream,
    data: DataEksporProduksi
) {
    val permintaan = data.permintaan
    val mulai = permintaan.tanggalMulai
    val akhir = permintaan.tanggalAkhir

    // Tanggal dari formulir laporan dan data produksi memakai yyyy-MM-dd.
    tulisLembarSirabas(output, "Produksi") { baris ->
        baris(listOf("PT. Sri Tani Berkah"))
        baris(listOf(permintaan.jenis))

        when (permintaan.jenis) {
            "Bahan baku masuk" -> {
                baris(listOf("Periode", mulai, akhir))
                baris(
                    listOf(
                        "Kode lot", "Tanggal", "Jenis bahan",
                        "Pemasok", "Asal", "Berat masuk (kg)"
                    )
                )

                val penerimaanPeriode = data.penerimaan
                    .filter { it.tanggal in mulai..akhir }
                    .sortedBy { it.tanggal }

                penerimaanPeriode.forEach { bahan ->
                    baris(
                        listOf(
                            bahan.kode, bahan.tanggal, bahan.jenis,
                            bahan.supplier, bahan.asalDaerah,
                            bahan.beratKg.toString().toBigDecimal()
                        )
                    )
                }

                val total = penerimaanPeriode.sumOf {
                    it.beratKg.toString().toBigDecimal()
                }
                baris(listOf("TOTAL", "", "", "", "", total))
            }

            "Hasil produksi" -> {
                baris(listOf("Periode", mulai, akhir))
                baris(
                    listOf(
                        "Batch", "Tanggal", "Lot bahan", "Kategori",
                        "Produk", "Kemasan", "Berat (kg)"
                    )
                )

                val batchPeriode = data.batch
                    .filter { it.tanggal in mulai..akhir }
                    .sortedBy { it.tanggal }

                if (batchPeriode.isEmpty()) {
                    baris(listOf("Belum ada produksi pada periode ini."))
                }

                batchPeriode.forEach { batch ->
                    baris(
                        listOf(
                            batch.kode, batch.tanggal, batch.kodeBahan,
                            "Bahan dipakai", "", "", batch.beratInput
                        )
                    )

                    batch.rincian.hasilUtama.forEach { hasil ->
                        baris(
                            listOf(
                                batch.kode, batch.tanggal, batch.kodeBahan,
                                "Hasil utama", hasil.namaProduk,
                                hasil.kemasan, hasil.beratKg
                            )
                        )
                    }

                    batch.rincian.hasilSamping.forEach { hasil ->
                        baris(
                            listOf(
                                batch.kode, batch.tanggal, batch.kodeBahan,
                                "Hasil samping", hasil.namaProduk,
                                "Curah", hasil.beratKg
                            )
                        )
                    }

                    baris(
                        listOf(
                            batch.kode, batch.tanggal, batch.kodeBahan,
                            "Susut", "", "", batch.susut
                        )
                    )
                }
            }

            "Stok gudang" -> {
                baris(listOf("Saldo per tanggal", akhir))
                baris(listOf("Dasar perhitungan", "Penerimaan dan produksi tercatat"))
                baris(listOf("Kategori", "Lot atau produk", "Kemasan", "Berat (kg)"))

                // Stok akhir juga mencakup penerimaan dan batch sebelum tanggal mulai.
                val batchSampaiAkhir = data.batch.filter { it.tanggal <= akhir }
                val lotSampaiAkhir = data.penerimaan.filter { it.tanggal <= akhir }

                lotSampaiAkhir.forEach { bahan ->
                    baris(
                        listOf(
                            "Bahan baku", bahan.kode, bahan.jenis,
                            sisaLotKg(bahan, batchSampaiAkhir)
                        )
                    )
                }

                val utama = batchSampaiAkhir
                    .flatMap { it.rincian.hasilUtama }
                    .groupBy { it.namaProduk to it.kemasan }

                utama.forEach { (produk, hasil) ->
                    baris(
                        listOf(
                            "Hasil utama", produk.first, produk.second,
                            hasil.sumOf { it.beratKg }
                        )
                    )
                }

                val samping = batchSampaiAkhir
                    .flatMap { it.rincian.hasilSamping }
                    .groupBy { it.namaProduk }

                samping.forEach { (nama, hasil) ->
                    baris(
                        listOf(
                            "Hasil samping", nama, "Curah",
                            hasil.sumOf { it.beratKg }
                        )
                    )
                }
            }

            else -> error("Jenis laporan tidak dikenali: ${permintaan.jenis}")
        }
    }
}