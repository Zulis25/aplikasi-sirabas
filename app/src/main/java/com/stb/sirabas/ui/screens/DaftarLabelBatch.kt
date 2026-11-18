package com.stb.sirabas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.math.BigInteger

private val polaKgLabel = Regex(
    """^(\d+(?:[.,]\d+)?)\s*kg\b""",
    RegexOption.IGNORE_CASE
)

private fun angkaLabel(nilai: BigDecimal): String =
    nilai.stripTrailingZeros().toPlainString().replace('.', ',')

@Composable
fun DaftarLabelBatch(batch: BatchProduksiUi) {
    var terbuka by rememberSaveable(batch.kode) { mutableStateOf(false) }
    var indeksProduk by rememberSaveable(batch.kode) { mutableStateOf(0) }
    var nomorKarung by rememberSaveable(batch.kode) { mutableStateOf("1") }

    val hasilUtama = batch.rincian.hasilUtama

    OutlinedButton(
        onClick = {
            indeksProduk = 0
            nomorKarung = "1"
            terbuka = true
        },
        enabled = hasilUtama.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Lihat label karung")
    }

    TombolUnduhLabelBatch(batch = batch)

    if (!terbuka) return

    val produk = hasilUtama.getOrNull(indeksProduk) ?: return
    val beratKemasan = remember(produk.kemasan) {
        polaKgLabel.find(produk.kemasan.trim())
            ?.groupValues
            ?.get(1)
            ?.replace(',', '.')
            ?.toBigDecimalOrNull()
            ?.takeIf { it.signum() > 0 }
    }

    val jumlahKarung = beratKemasan?.let {
        produk.beratKg.divideToIntegralValue(it).toBigInteger()
    } ?: BigInteger.ZERO

    val nomor = nomorKarung.toBigIntegerOrNull() ?: BigInteger.ONE

    AlertDialog(
        onDismissRequest = { terbuka = false },
        title = { Text("Label karung hasil produksi") },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Batch ${batch.kode} · Lot ${batch.kodeBahan}")
                    Text(
                        text = "Tanggal produksi: ${batch.tanggal}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    HorizontalDivider()
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${produk.namaProduk} · ${produk.kemasan}",
                            fontWeight = FontWeight.Bold
                        )
                        Text("Berat hasil: ${angkaLabel(produk.beratKg)} kg")

                        if (beratKemasan == null) {
                            Text("Ukuran kemasan belum bisa dibaca.")
                        } else {
                            Text("Kemasan penuh: $jumlahKarung karung")
                            val sisa = produk.beratKg.remainder(beratKemasan)
                            if (sisa.signum() > 0) {
                                Text("Sisa curah: ${angkaLabel(sisa)} kg")
                            }
                        }
                    }
                }

                if (jumlahKarung.signum() > 0) {
                    item {
                        HorizontalDivider()

                        // Nomor baris produk membedakan dua kemasan dalam satu batch.
                        val baris = (indeksProduk + 1).toString().padStart(2, '0')
                        val urutan = nomor.toString().padStart(3, '0')
                        val kodeKarung = "${batch.kode}-P$baris-K$urutan"

                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Karung $nomor dari $jumlahKarung")
                            Text(
                                text = kodeKarung,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )

                            QrDanCetakLabelKarung(
                                kodeKarung = kodeKarung,
                                kodeBatch = batch.kode,
                                kodeLot = batch.kodeBahan,
                                tanggal = batch.tanggal,
                                namaProduk = produk.namaProduk,
                                kemasan = produk.kemasan
                            )
                        }
                    }

                    item {
                        if (nomor > BigInteger.ONE) {
                            TextButton(
                                onClick = {
                                    nomorKarung = (nomor - BigInteger.ONE).toString()
                                }
                            ) {
                                Text("‹ Karung sebelumnya")
                            }
                        }

                        if (nomor < jumlahKarung) {
                            TextButton(
                                onClick = {
                                    nomorKarung = (nomor + BigInteger.ONE).toString()
                                }
                            ) {
                                Text("Karung berikutnya ›")
                            }
                        }
                    }
                }

                if (hasilUtama.size > 1) {
                    item {
                        HorizontalDivider()
                        Text("Produk ${indeksProduk + 1} dari ${hasilUtama.size}")

                        if (indeksProduk > 0) {
                            TextButton(
                                onClick = {
                                    indeksProduk--
                                    nomorKarung = "1"
                                }
                            ) {
                                Text("‹ Produk sebelumnya")
                            }
                        }

                        if (indeksProduk < hasilUtama.lastIndex) {
                            TextButton(
                                onClick = {
                                    indeksProduk++
                                    nomorKarung = "1"
                                }
                            ) {
                                Text("Produk berikutnya ›")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { terbuka = false }) {
                Text("Tutup")
            }
        }
    )
}