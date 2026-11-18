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
    val cocok = polaKodeKarung.matchEntire(kode) ?: return null
    val (kodeBatch, teksProduk, teksKarung) = cocok.destructured

    val nomorProduk = teksProduk.toIntOrNull() ?: return null
    val nomorKarung = teksKarung.toBigIntegerOrNull() ?: return null
    if (nomorProduk <= 0 || nomorKarung < BigInteger.ONE) return null

    // Terima hanya bentuk kode yang benar-benar dibuat oleh pencetak label.
    val kodeAsli = "$kodeBatch-P${nomorProduk.toString().padStart(2, '0')}" +
            "-K${nomorKarung.toString().padStart(3, '0')}"
    if (kode != kodeAsli) return null

    val batch = batchProduksi.singleOrNull { it.kode == kodeBatch } ?: return null
    val produk = batch.rincian.hasilUtama.getOrNull(nomorProduk - 1) ?: return null

    val beratKemasan = polaBeratKarung.find(produk.kemasan.trim())
        ?.groupValues?.get(1)
        ?.replace(',', '.')
        ?.toBigDecimalOrNull()
        ?: return null

    if (beratKemasan.signum() <= 0) return null
    val jumlahPenuh = produk.beratKg.divideToIntegralValue(beratKemasan).toBigInteger()

    return produk.takeIf { nomorKarung <= jumlahPenuh }
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