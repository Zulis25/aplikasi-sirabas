package com.stb.sirabas.ui.screens

import java.math.BigDecimal

data class BatchProduksiUi(
    val kode: String,
    val tanggal: String,
    val kodeBahan: String,
    val beratInput: BigDecimal,
    val rincian: RincianHasilProduksiUi
) {
    // Dipakai oleh HitungProduksiScreen yang sekarang masih mengirim
    // satu produk utama dan satu hasil samping.
    constructor(
        kode: String,
        tanggal: String,
        kodeBahan: String,
        beratInput: BigDecimal,
        namaUtama: String,
        kemasan: String,
        beratUtama: BigDecimal,
        namaSamping: String,
        beratSamping: BigDecimal
    ) : this(
        kode = kode,
        tanggal = tanggal,
        kodeBahan = kodeBahan,
        beratInput = beratInput,
        rincian = RincianHasilProduksiUi(
            hasilUtama = if (beratUtama.signum() == 0) {
                emptyList()
            } else {
                listOf(
                    HasilUtamaProduksiUi(
                        namaProduk = namaUtama,
                        kemasan = kemasan,
                        beratKg = beratUtama
                    )
                )
            },
            hasilSamping = if (beratSamping.signum() == 0) {
                emptyList()
            } else {
                listOf(
                    HasilSampingProduksiUi(
                        namaProduk = namaSamping,
                        beratKg = beratSamping
                    )
                )
            }
        )
    )

    // Total dihitung dari baris hasil agar ringkasan selalu sesuai.
    val beratUtama: BigDecimal
        get() = rincian.totalUtamaKg

    val beratSamping: BigDecimal
        get() = rincian.totalSampingKg

    val susut: BigDecimal
        get() = beratInput - beratUtama - beratSamping

    // Nama ini masih bisa dibaca oleh layar lama untuk tampilan ringkas.
    // Untuk rincian stok per produk, gunakan setiap baris dalam rincian.
    val namaUtama: String
        get() = rincian.hasilUtama
            .map { it.namaProduk }
            .distinct()
            .joinToString(", ")

    val kemasan: String
        get() = rincian.hasilUtama
            .map { it.kemasan }
            .distinct()
            .joinToString(", ")

    val namaSamping: String
        get() = rincian.hasilSamping
            .map { it.namaProduk }
            .distinct()
            .joinToString(", ")
}

// Satu batch mengurangi lot sebanyak berat bahan yang dipakai.
fun sisaLotKg(
    bahan: BahanBakuUi,
    daftarBatch: List<BatchProduksiUi>
): BigDecimal {
    val sudahDipakai = daftarBatch
        .filter { it.kodeBahan == bahan.kode }
        .fold(BigDecimal.ZERO) { jumlah, batch ->
            jumlah + batch.beratInput
        }

    return BigDecimal.valueOf(bahan.beratKg) - sudahDipakai
}