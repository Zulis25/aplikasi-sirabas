package com.stb.sirabas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PengirimanKarungUi(
    val nomorPesanan: String,
    val pelanggan: String,
    val tanggalKeluar: String,
    val kodeKarung: List<String>
)

internal fun tanggalKeluarHariIni(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

// Kurangi berat pada baris produk dan batch asal QR tersebut.
// Fungsi ini menghasilkan salinan untuk tampilan stok.
internal fun hasilUtamaSetelahPengiriman(
    batch: BatchProduksiUi,
    kodeKeluar: Set<String>
): List<HasilUtamaProduksiUi> {
    return batch.rincian.hasilUtama.mapIndexed { indeks, hasil ->
        val nomorProduk = (indeks + 1).toString().padStart(2, '0')
        val awalan = "${batch.kode}-P$nomorProduk-K"
        val jumlahKeluar = kodeKeluar.count { it.startsWith(awalan) }

        val beratKemasan = Regex(
            """^(\d+(?:[.,]\d+)?)\s*kg\b""",
            RegexOption.IGNORE_CASE
        ).find(hasil.kemasan.trim())
            ?.groupValues?.get(1)
            ?.replace(',', '.')
            ?.toBigDecimalOrNull()

        if (jumlahKeluar == 0 || beratKemasan == null) {
            hasil
        } else {
            val beratKeluar = beratKemasan * jumlahKeluar.toBigDecimal()
            hasil.copy(beratKg = hasil.beratKg - beratKeluar)
        }
    }
}

@Composable
internal fun DialogPeriksaKarung(
    pesanan: NotifikasiPesananProduksiUi,
    karungPesanan: Map<String, KarungDipindaiUi>,
    onKembali: () -> Unit,
    onKonfirmasi: () -> Unit
) {
    val jumlahDipesan = pesanan.daftarItem.sumOf { it.jumlahPesanan }
    val lengkap = jumlahDipesan > 0 &&
            karungPesanan.size == jumlahDipesan &&
            pesanan.daftarItem.all { item ->
                karungPesanan.values.count {
                    it.namaProduk == item.namaProduk && it.kemasan == item.kemasan
                } == item.jumlahPesanan
            }

    AlertDialog(
        onDismissRequest = onKembali,
        title = { Text("Periksa pengiriman") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 390.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(pesanan.pelanggan, fontWeight = FontWeight.Bold)
                        Text(pesanan.nomorPesanan)
                        HorizontalDivider()
                    }
                }

                items(pesanan.daftarItem) { item ->
                    Text(
                        "${item.namaProduk} · ${item.kemasan} " +
                                "× ${item.jumlahPesanan} karung"
                    )
                }

                item {
                    HorizontalDivider()
                    Text("Kode karung yang akan keluar", fontWeight = FontWeight.SemiBold)
                }

                items(karungPesanan.keys.sorted()) { kode ->
                    Text(kode)
                }

                item {
                    Text(
                        if (lengkap) "Konfirmasi setelah seluruh karung dimuat."
                        else "Jumlah atau jenis karung belum sesuai dengan pesanan."
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onKonfirmasi, enabled = lengkap) {
                Text("Konfirmasi barang keluar")
            }
        },
        dismissButton = {
            TextButton(onClick = onKembali) { Text("Kembali") }
        }
    )
}