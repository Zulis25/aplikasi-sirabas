package com.stb.sirabas.ui.screens

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.math.BigInteger
import java.util.Locale

data class KarungDipindaiUi(
    val nomorPesanan: String,
    val namaProduk: String,
    val kemasan: String
)

private val polaKodeKarung = Regex("""(.+)-P([0-9]+)-K([0-9]+)""")
private val polaBeratKarung = Regex(
    """^(\d+(?:[.,]\d+)?)\s*kg\b""",
    RegexOption.IGNORE_CASE
)

private fun namaUntukPindai(nama: String): String =
    nama.trim().lowercase(Locale.ROOT).removePrefix("beras ").trim()

private fun samaProduk(
    namaPertama: String,
    kemasanPertama: String,
    namaKedua: String,
    kemasanKedua: String
): Boolean =
    namaUntukPindai(namaPertama) == namaUntukPindai(namaKedua) &&
            kemasanPertama.trim().equals(kemasanKedua.trim(), ignoreCase = true)

private fun cariProdukDariQr(
    kode: String,
    batchProduksi: List<BatchProduksiUi>
): HasilUtamaProduksiUi? {
    val cleanKode = kode.trim()

    // 1. Format dari Server API: {kodeBatch}-K{nomorKarung} (misal: PR/002-K0001)
    val matchApi = Regex("""^(.+)-K(\d+)$""").matchEntire(cleanKode)
    if (matchApi != null) {
        val (kodeBatch, _) = matchApi.destructured
        val batch = batchProduksi.firstOrNull { it.kode == kodeBatch }
        if (batch != null) {
            return batch.rincian.hasilUtama.firstOrNull()
        }
    }

    // 2. Format Lokal: {kodeBatch}-P{nomorProduk}-K{nomorKarung}
    val matchLegacy = Regex("""^(.+)-P(\d+)-K(\d+)$""").matchEntire(cleanKode)
    if (matchLegacy != null) {
        val (kodeBatch, teksProduk, _) = matchLegacy.destructured
        val nomorProduk = teksProduk.toIntOrNull() ?: return null
        val batch = batchProduksi.firstOrNull { it.kode == kodeBatch } ?: return null
        return batch.rincian.hasilUtama.getOrNull(nomorProduk - 1)
    }

    return null
}

@Composable
fun PersiapanDenganPemindai(
    daftarPesanan: List<NotifikasiPesananProduksiUi>,
    batchProduksi: List<BatchProduksiUi>,
    hasilPindai: MutableMap<String, KarungDipindaiUi>,
    onPeriksaPengiriman: (NotifikasiPesananProduksiUi) -> Unit
) {
    val context = LocalContext.current
    val scanner = remember(context) {
        val opsi = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        GmsBarcodeScanning.getClient(context, opsi)
    }

    var sedangMemindai by remember { mutableStateOf(false) }
    val pesananKini by rememberUpdatedState(daftarPesanan)
    val batchKini by rememberUpdatedState(batchProduksi)

    fun beriPesan(teks: String) {
        Toast.makeText(context, teks, Toast.LENGTH_LONG).show()
    }

    fun jumlahUntuk(nomor: String, item: ItemPersiapanProduksiUi): Int =
        hasilPindai.values.count { karung ->
            karung.nomorPesanan == nomor &&
                    samaProduk(
                        karung.namaProduk, karung.kemasan,
                        item.namaProduk, item.kemasan
                    )
        }

    fun catatQr(nomorPesanan: String, isiQr: String?) {
        val kode = isiQr.orEmpty().trim()
        val pesanan = pesananKini.firstOrNull { it.nomorPesanan == nomorPesanan }

        if (pesanan == null) {
            beriPesan("Pesanan ini sudah tidak tersedia.")
            return
        }

        val sudahAda = hasilPindai[kode]
        if (sudahAda != null) {
            beriPesan("Karung ini sudah dipindai untuk ${sudahAda.nomorPesanan}.")
            return
        }

        val produk = cariProdukDariQr(kode, batchKini)
        if (produk == null) {
            beriPesan("Kode tidak cocok dengan label karung pada batch produksi.")
            return
        }

        val item = pesanan.daftarItem.firstOrNull {
            samaProduk(it.namaProduk, it.kemasan, produk.namaProduk, produk.kemasan)
        }

        if (item == null) {
            beriPesan("${produk.namaProduk} ${produk.kemasan} tidak ada di pesanan ini.")
            return
        }

        val jumlah = item.jumlahDipindai + jumlahUntuk(nomorPesanan, item)
        if (jumlah >= item.jumlahPesanan) {
            beriPesan("Jumlah ${item.namaProduk} ${item.kemasan} sudah lengkap.")
            return
        }

        hasilPindai[kode] = KarungDipindaiUi(
            nomorPesanan = nomorPesanan,
            namaProduk = item.namaProduk,
            kemasan = item.kemasan
        )

        beriPesan("$kode diterima · ${jumlah + 1} dari ${item.jumlahPesanan} karung.")
    }

    val pesananUntukLayar = daftarPesanan.map { pesanan ->
        pesanan.copy(
            daftarItem = pesanan.daftarItem.map { item ->
                item.copy(
                    jumlahDipindai = item.jumlahDipindai +
                            jumlahUntuk(pesanan.nomorPesanan, item)
                )
            }
        )
    }

    AlurPersiapanPesananScreen(
        daftarPesanan = pesananUntukLayar,
        onPindaiKarung = { pesanan ->
            if (!sedangMemindai) {
                sedangMemindai = true
                try {
                    scanner.startScan()
                        .addOnSuccessListener { barcode ->
                            catatQr(pesanan.nomorPesanan, barcode.rawValue)
                        }
                        .addOnFailureListener {
                            beriPesan("Pemindai belum bisa dibuka. Periksa Google Play services dan coba lagi.")
                        }
                        .addOnCompleteListener {
                            sedangMemindai = false
                        }
                } catch (e: RuntimeException) {
                    sedangMemindai = false
                    beriPesan("Pemindai tidak tersedia pada perangkat ini.")
                }
            }
        },
        onPeriksaPengiriman = onPeriksaPengiriman
    )
}