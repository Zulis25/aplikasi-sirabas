package com.stb.sirabas.ui.screens

import java.math.BigDecimal

// Satu baris hasil utama, misalnya Premium, 5 kg, total 100 kg.
data class HasilUtamaProduksiUi(
    val namaProduk: String,
    val kemasan: String,
    val beratKg: BigDecimal
)

// Satu baris hasil samping, misalnya Sekam sebanyak 5 kg.
data class HasilSampingProduksiUi(
    val namaProduk: String,
    val beratKg: BigDecimal
)

// Seluruh hasil yang berasal dari satu kali penggilingan.
data class RincianHasilProduksiUi(
    val hasilUtama: List<HasilUtamaProduksiUi>,
    val hasilSamping: List<HasilSampingProduksiUi>
) {
    val totalUtamaKg: BigDecimal
        get() = hasilUtama.fold(BigDecimal.ZERO) { total, hasil ->
            total + hasil.beratKg
        }

    val totalSampingKg: BigDecimal
        get() = hasilSamping.fold(BigDecimal.ZERO) { total, hasil ->
            total + hasil.beratKg
        }
}