package com.stb.sirabas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StokBahanBakuScreen(
    stokTersediaKg: Double?,
    penerimaanBaru: List<BahanBakuUi>,
    batchProduksi: List<BatchProduksiUi> = emptyList()
) {
    val formatAngka = remember {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 3
        }
    }

    // Ambil seluruh baris hasil utama, lalu kelompokkan per produk dan kemasan.
    val kelompokUtama = batchProduksi
        .flatMap { batch -> batch.rincian.hasilUtama }
        .groupBy { baris ->
            baris.namaProduk.trim().lowercase(Locale.ROOT) to
                    baris.kemasan.trim().lowercase(Locale.ROOT)
        }

    // Satu batch dapat menyumbang berat ke beberapa jenis hasil samping.
    val kelompokSamping = batchProduksi
        .flatMap { batch -> batch.rincian.hasilSamping }
        .groupBy { baris ->
            baris.namaProduk.trim().lowercase(Locale.ROOT)
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Stok Gudang",
                color = AccessColors.Ink,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF123F2E),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Stok bahan baku tersedia",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    // Saldo dari halaman induk sudah dikurangi pemakaian produksi.
                    Text(
                        text = stokTersediaKg?.let {
                            "${formatAngka.format(it)} kg"
                        } ?: "Saldo awal belum tersedia",
                        color = Color.White,
                        fontSize = if (stokTersediaKg != null) 32.sp else 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            JudulBagianStok("Stok hasil utama")
        }

        if (kelompokUtama.isEmpty()) {
            item {
                Text("Belum ada hasil utama tercatat.", color = AccessColors.Muted)
            }
        } else {
            items(kelompokUtama.values.toList()) { barisProduk ->
                val produk = barisProduk.first()
                val totalKg = barisProduk.fold(BigDecimal.ZERO) { jumlah, baris ->
                    jumlah + baris.beratKg
                }

                KartuSaldoProduk(
                    nama = "${produk.namaProduk} · ${produk.kemasan}",
                    saldo = "${formatAngka.format(totalKg)} kg"
                )
            }
        }

        item {
            JudulBagianStok("Stok hasil samping")
        }

        if (kelompokSamping.isEmpty()) {
            item {
                Text("Belum ada hasil samping tercatat.", color = AccessColors.Muted)
            }
        } else {
            items(kelompokSamping.values.toList()) { barisProduk ->
                val totalKg = barisProduk.fold(BigDecimal.ZERO) { jumlah, baris ->
                    jumlah + baris.beratKg
                }

                KartuSaldoProduk(
                    nama = barisProduk.first().namaProduk,
                    saldo = "${formatAngka.format(totalKg)} kg"
                )
            }
        }

        if (batchProduksi.isNotEmpty()) {
            item {
                JudulBagianStok("Riwayat produksi")
            }

            items(
                items = batchProduksi.asReversed(),
                key = { batch -> "produksi-${batch.kode}" }
            ) { batch ->
                AccessCard {
                    Text(
                        text = "${batch.kode} · ${batch.tanggal}",
                        fontWeight = FontWeight.Bold,
                        color = AccessColors.Ink
                    )
                    Text("Lot bahan: ${batch.kodeBahan}")
                    Text("Bahan dipakai: ${formatAngka.format(batch.beratInput)} kg")

                    if (batch.rincian.hasilUtama.isNotEmpty()) {
                        Text(
                            text = "Hasil utama · ${formatAngka.format(batch.beratUtama)} kg",
                            fontWeight = FontWeight.SemiBold,
                            color = AccessColors.Ink
                        )
                        batch.rincian.hasilUtama.forEach { baris ->
                            Text(
                                "${baris.namaProduk} · ${baris.kemasan} — " +
                                        "${formatAngka.format(baris.beratKg)} kg"
                            )
                        }
                    }

                    if (batch.rincian.hasilSamping.isNotEmpty()) {
                        Text(
                            text = "Hasil samping · ${formatAngka.format(batch.beratSamping)} kg",
                            fontWeight = FontWeight.SemiBold,
                            color = AccessColors.Ink
                        )
                        batch.rincian.hasilSamping.forEach { baris ->
                            Text(
                                "${baris.namaProduk} — " +
                                        "${formatAngka.format(baris.beratKg)} kg"
                            )
                        }
                    }

                    Text(
                        text = "Susut: ${formatAngka.format(batch.susut)} kg",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            JudulBagianStok("Penerimaan baru")
        }

        if (penerimaanBaru.isEmpty()) {
            item {
                AccessCard {
                    Text("Belum ada penerimaan baru.", color = AccessColors.Muted)
                }
            }
        } else {
            items(
                items = penerimaanBaru.asReversed(),
                key = { bahan -> "penerimaan-${bahan.kode}" }
            ) { bahan ->
                val sisaKg = sisaLotKg(bahan, batchProduksi)

                AccessCard {
                    Text(
                        text = "${bahan.kode} · ${bahan.tanggal}",
                        color = AccessColors.Muted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = bahan.jenis,
                        color = AccessColors.Ink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${bahan.supplier} · ${bahan.asalDaerah}",
                        color = AccessColors.Muted
                    )
                    Text("Berat diterima: ${formatAngka.format(bahan.beratKg)} kg")
                    Text(
                        text = "Sisa lot: ${formatAngka.format(sisaKg)} kg",
                        color = AccessColors.Green,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun JudulBagianStok(judul: String) {
    Text(
        text = judul,
        color = AccessColors.Ink,
        fontSize = 19.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun KartuSaldoProduk(
    nama: String,
    saldo: String
) {
    AccessCard {
        Text(
            text = nama,
            color = AccessColors.Ink,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = saldo,
            color = AccessColors.Green,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
@androidx.compose.ui.tooling.preview.Preview(
    name = "Stok Gudang · Petugas Produksi",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PreviewStokGudangProduksi() {
    val contohBatch = BatchProduksiUi(
        kode = "PR/001",
        tanggal = "2026-11-11",
        kodeBahan = "BM/025",
        beratInput = java.math.BigDecimal("200"),
        rincian = RincianHasilProduksiUi(
            hasilUtama = listOf(
                HasilUtamaProduksiUi(
                    namaProduk = "Premium",
                    kemasan = "5 kg",
                    beratKg = java.math.BigDecimal("180")
                )
            ),
            hasilSamping = listOf(
                HasilSampingProduksiUi(
                    namaProduk = "Dedak Halus",
                    beratKg = java.math.BigDecimal("8")
                ),
                HasilSampingProduksiUi(
                    namaProduk = "Sekam",
                    beratKg = java.math.BigDecimal("5")
                )
            )
        )
    )

    com.stb.sirabas.ui.theme.SIRABASTheme {
        StokBahanBakuScreen(
            stokTersediaKg = 300.0,
            penerimaanBaru = emptyList(),
            batchProduksi = listOf(contohBatch)
        )
    }
}