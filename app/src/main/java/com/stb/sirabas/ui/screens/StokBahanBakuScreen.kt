package com.stb.sirabas.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
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
    batchProduksi: List<BatchProduksiUi> = emptyList(),
    kodeSudahKeluar: Set<String> = emptySet()
) {
    val warna = MaterialTheme.colorScheme

    var kategori by rememberSaveable {
        mutableStateOf("Semua")
    }

    val pilihanKategori = listOf(
        "Semua",
        "Hasil utama",
        "Hasil samping",
        "Riwayat penerimaan",
        "Riwayat produksi"
    )

    val formatAngka = remember {
        NumberFormat.getNumberInstance(
            Locale.forLanguageTag("id-ID")
        ).apply {
            maximumFractionDigits = 3
        }
    }

    fun kg(nilai: Number): String {
        return "${formatAngka.format(nilai)} kg"
    }

    val kelompokUtama = batchProduksi
        .flatMap { batch -> hasilUtamaSetelahPengiriman(batch, kodeSudahKeluar) }
        .groupBy {
            it.namaProduk.trim().lowercase(Locale.ROOT) to
                    it.kemasan.trim().lowercase(Locale.ROOT)
        }

    val kelompokSamping = batchProduksi
        .flatMap { it.rincian.hasilSamping }
        .groupBy {
            it.namaProduk.trim().lowercase(Locale.ROOT)
        }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = warna.background,
        contentColor = warna.onBackground
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Stok Gudang",
                        color = warna.onBackground,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Pantau bahan baku dan hasil produksi.",
                        color = warna.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    HorizontalDivider(
                        color = warna.outlineVariant
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pilihanKategori.forEach { pilihan ->
                        FilterChip(
                            selected = kategori == pilihan,
                            onClick = {
                                kategori = pilihan
                            },
                            label = {
                                Text(pilihan)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor =
                                    warna.secondaryContainer,
                                selectedLabelColor =
                                    warna.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            if (kategori == "Semua") {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = warna.primaryContainer,
                        contentColor = warna.onPrimaryContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Stok bahan baku tersedia",
                                fontSize = 14.sp
                            )

                            Text(
                                text = stokTersediaKg?.let {
                                    kg(it)
                                } ?: "Saldo belum tersedia",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (
                kategori == "Semua" ||
                kategori == "Hasil utama"
            ) {
                item {
                    JudulBagianGudang("Stok hasil utama")
                }

                if (kelompokUtama.isEmpty()) {
                    item {
                        PesanKosongGudang(
                            "Belum ada hasil utama tercatat."
                        )
                    }
                } else {
                    items(
                        items = kelompokUtama.values.toList()
                    ) { barisProduk ->
                        val produk = barisProduk.first()

                        val totalKg = barisProduk.fold(
                            BigDecimal.ZERO
                        ) { total, baris ->
                            total + baris.beratKg
                        }

                        AccessCard {
                            Text(
                                text = "${produk.namaProduk} · " +
                                        produk.kemasan,
                                color = warna.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = kg(totalKg),
                                color = warna.primary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            HorizontalDivider(
                                color = warna.outlineVariant
                            )

                            Text(
                                text = rincianKemasanGudang(
                                    totalKg = totalKg,
                                    kemasan = produk.kemasan
                                ),
                                color = warna.onSurfaceVariant,
                                fontSize = 14.sp,
                                lineHeight = 21.sp
                            )
                        }
                    }
                }
            }

            if (
                kategori == "Semua" ||
                kategori == "Hasil samping"
            ) {
                item {
                    JudulBagianGudang("Stok hasil samping")
                }

                if (kelompokSamping.isEmpty()) {
                    item {
                        PesanKosongGudang(
                            "Belum ada hasil samping tercatat."
                        )
                    }
                } else {
                    items(
                        items = kelompokSamping.values.toList()
                    ) { barisProduk ->
                        val totalKg = barisProduk.fold(
                            BigDecimal.ZERO
                        ) { total, baris ->
                            total + baris.beratKg
                        }

                        AccessCard {
                            Text(
                                text = barisProduk.first().namaProduk,
                                color = warna.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = kg(totalKg),
                                color = warna.primary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Dicatat berdasarkan berat (kg)",
                                color = warna.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            if (
                kategori == "Semua" ||
                kategori == "Riwayat penerimaan"
            ) {
                item {
                    JudulBagianGudang(
                        "Riwayat penerimaan bahan baku"
                    )
                }

                if (penerimaanBaru.isEmpty()) {
                    item {
                        PesanKosongGudang(
                            "Belum ada penerimaan bahan baku."
                        )
                    }
                } else {
                    items(
                        items = penerimaanBaru.distinctBy { it.kode }.sortedByDescending {
                            it.tanggal
                        }
                    ) { bahan ->
                        val sisa = sisaLotKg(
                            bahan,
                            batchProduksi
                        )

                        AccessCard {
                            Text(
                                text = "${bahan.kode} · ${bahan.tanggal}",
                                color = warna.onSurfaceVariant,
                                fontSize = 13.sp
                            )

                            Text(
                                text = bahan.jenis,
                                color = warna.onSurface,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = bahan.supplier,
                                color = warna.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (bahan.asalDaerah.isNotBlank()) {
                                Text(
                                    text = bahan.asalDaerah,
                                    color = warna.onSurfaceVariant
                                )
                            }

                            HorizontalDivider(
                                color = warna.outlineVariant
                            )

                            Text(
                                text = "Berat diterima: ${kg(bahan.beratKg)}",
                                color = warna.onSurface
                            )

                            Text(
                                text = "Sisa lot: ${kg(sisa)}",
                                color = warna.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (
                kategori == "Semua" ||
                kategori == "Riwayat produksi"
            ) {
                item {
                    JudulBagianGudang("Riwayat produksi")
                }

                if (batchProduksi.isEmpty()) {
                    item {
                        PesanKosongGudang(
                            "Belum ada produksi tercatat."
                        )
                    }
                } else {
                    items(
                        items = batchProduksi.distinctBy { it.kode }.asReversed()
                    ) { batch ->
                        AccessCard {
                            Text(
                                text = "${batch.kode} · ${batch.tanggal}",
                                color = warna.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Lot bahan: ${batch.kodeBahan}",
                                color = warna.onSurfaceVariant
                            )

                            Text(
                                text = "Bahan dipakai: ${kg(batch.beratInput)}",
                                color = warna.onSurface
                            )

                            DaftarLabelBatch(batch = batch)

                            HorizontalDivider(
                                color = warna.outlineVariant
                            )

                            if (batch.rincian.hasilUtama.isNotEmpty()) {
                                Text(
                                    text = "Hasil utama",
                                    color = warna.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )

                                batch.rincian.hasilUtama.forEach { hasil ->
                                    Text(
                                        text = "${hasil.namaProduk} · " +
                                                "${hasil.kemasan}: " +
                                                kg(hasil.beratKg),
                                        color = warna.onSurfaceVariant
                                    )
                                }
                            }

                            if (batch.rincian.hasilSamping.isNotEmpty()) {
                                Text(
                                    text = "Hasil samping",
                                    color = warna.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )

                                batch.rincian.hasilSamping.forEach { hasil ->
                                    Text(
                                        text = "${hasil.namaProduk}: " +
                                                kg(hasil.beratKg),
                                        color = warna.onSurfaceVariant
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = warna.outlineVariant
                            )

                            Text(
                                text = "Susut: ${kg(batch.susut)}",
                                color = warna.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JudulBagianGudang(judul: String) {
    Text(
        text = judul,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun PesanKosongGudang(pesan: String) {
    AccessCard {
        Text(
            text = pesan,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}

private fun rincianKemasanGudang(
    totalKg: BigDecimal,
    kemasan: String
): String {
    val beratPerKemasan = Regex(
        pattern = """(\d+(?:[.,]\d+)?)\s*kg""",
        option = RegexOption.IGNORE_CASE
    )
        .find(kemasan)
        ?.groupValues
        ?.get(1)
        ?.replace(',', '.')
        ?.toBigDecimalOrNull()
        ?.takeIf {
            it.signum() > 0
        }
        ?: return "Jumlah kemasan belum dapat dihitung."

    val jumlahUtuh = totalKg.divideToIntegralValue(
        beratPerKemasan
    )

    val sisaCurah = totalKg.remainder(
        beratPerKemasan
    )

    val format = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("id-ID")
    ).apply {
        maximumFractionDigits = 3
    }

    val ringkasan = "Setara ${format.format(jumlahUtuh)} karung × " +
            "${format.format(beratPerKemasan)} kg"

    return if (sisaCurah.signum() > 0) {
        "$ringkasan\nSisa curah: ${format.format(sisaCurah)} kg"
    } else {
        ringkasan
    }
}