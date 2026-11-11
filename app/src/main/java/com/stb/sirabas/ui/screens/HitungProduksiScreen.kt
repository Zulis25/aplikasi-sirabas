package com.stb.sirabas.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Setiap kombinasi produk dan kemasan mewakili satu varian stok.
private val kemasanPerProduk = linkedMapOf(
    "Premium" to listOf("5 kg", "10 kg", "25 kg hijau", "25 kg biru"),
    "Medium" to listOf("25 kg"),
    "Beras" to listOf("50 kg")
)

private val daftarHasilSamping = listOf(
    "Dedak Halus",
    "Dedak Kasar",
    "Menir",
    "Reject",
    "Broken",
    "Sekam"
)
// Satu baris yang sedang diisi petugas.
// ID membuat setiap baris tetap dikenali saat baris lain dihapus.
private data class BarisHasilForm(
    val id: String = java.util.UUID.randomUUID().toString(),
    val produk: String = "",
    val kemasan: String = "",
    val berat: String = ""
) : java.io.Serializable

@Composable
fun HitungProduksiScreen(
    bahanTersedia: List<BahanBakuUi>,
    onKembali: () -> Unit,
    onKonfirmasi: (BatchProduksiUi) -> Unit
) {
    FormHitungProduksi(
        bahanTersedia = bahanTersedia,
        onKembali = onKembali,
        onKonfirmasi = onKonfirmasi
    )
}

@Composable
private fun FormHitungProduksi(
    bahanTersedia: List<BahanBakuUi>,
    onKembali: () -> Unit,
    onKonfirmasi: (BatchProduksiUi) -> Unit,
    kodeAwal: String = "",
    beratAwal: String = "",
    utamaAwal: List<BarisHasilForm> = listOf(BarisHasilForm()),
    sampingAwal: List<BarisHasilForm> = emptyList()
) {
    var kodeTerpilih by rememberSaveable {
        mutableStateOf(kodeAwal)
    }

    var beratInput by rememberSaveable(kodeTerpilih) {
        mutableStateOf(if (kodeTerpilih == kodeAwal) beratAwal else "")
    }

    var barisUtama by rememberSaveable(kodeTerpilih) {
        val awal = if (kodeTerpilih == kodeAwal) {
            ArrayList(utamaAwal)
        } else {
            arrayListOf(BarisHasilForm())
        }
        mutableStateOf(awal)
    }

    var barisSamping by rememberSaveable(kodeTerpilih) {
        val awal = if (kodeTerpilih == kodeAwal) {
            ArrayList(sampingAwal)
        } else {
            arrayListOf<BarisHasilForm>()
        }
        mutableStateOf(awal)
    }

    val sumber = bahanTersedia.firstOrNull { it.kode == kodeTerpilih }
    val batasSumber = sumber?.let { BigDecimal.valueOf(it.beratKg) }
    val input = beratProduksi(beratInput)

    val inputError = when {
        beratInput.isBlank() -> null
        input == null || input.signum() <= 0 ->
            "Masukkan berat bahan lebih dari 0 kg."
        batasSumber != null && input > batasSumber ->
            "Berat bahan melebihi stok lot ini."
        else -> null
    }

    // Hanya baris lengkap yang bisa dibuat menjadi rincian batch.
    val hasilUtama = barisUtama.mapNotNull { baris ->
        val berat = beratProduksi(baris.berat)
        if (berat == null || berat.signum() <= 0 ||
            baris.kemasan !in kemasanPerProduk[baris.produk].orEmpty()
        ) {
            null
        } else {
            HasilUtamaProduksiUi(baris.produk, baris.kemasan, berat)
        }
    }

    val hasilSamping = barisSamping.mapNotNull { baris ->
        val berat = beratProduksi(baris.berat)
        if (berat == null || berat.signum() <= 0 ||
            baris.produk !in daftarHasilSamping
        ) {
            null
        } else {
            HasilSampingProduksiUi(baris.produk, berat)
        }
    }

    val semuaBarisLengkap = hasilUtama.size == barisUtama.size &&
            hasilSamping.size == barisSamping.size
    val adaHasil = hasilUtama.isNotEmpty() || hasilSamping.isNotEmpty()
    val rincian = RincianHasilProduksiUi(hasilUtama, hasilSamping)
    val totalHasil = rincian.totalUtamaKg + rincian.totalSampingKg

    BackHandler(onBack = onKembali)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onKembali) {
            Text("← Kembali", color = AccessColors.Green)
        }

        Text(
            text = "Hasil Produksi",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = AccessColors.Ink
        )

        if (bahanTersedia.isEmpty()) {
            Text("Belum ada lot bahan yang tersedia.")
        } else {
            PilihanProduksi(
                label = "Bahan yang diproduksi",
                petunjuk = "Pilih lot bahan baku",
                nilai = kodeTerpilih,
                pilihan = bahanTersedia.map { it.kode },
                teksPilihan = { kode ->
                    val bahan = bahanTersedia.firstOrNull { it.kode == kode }
                    if (bahan == null) kode
                    else "${bahan.kode} · ${bahan.jenis} · ${bahan.supplier}"
                },
                onPilih = { kodeTerpilih = it }
            )
        }

        if (sumber != null && batasSumber != null) {
            AccessCard {
                Text(sumber.supplier, fontWeight = FontWeight.SemiBold)
                Text("${sumber.asalDaerah} · ${sumber.tanggal}")
                Text(
                    "Tersedia ${formatProduksi(batasSumber)} kg",
                    color = AccessColors.Green
                )
            }

            KolomBeratProduksi(
                label = "Berat bahan yang dipakai (kg)",
                nilai = beratInput,
                ubah = { beratInput = it },
                pesanError = inputError
            )

            BagianHasilProduksi(
                utama = true,
                daftar = barisUtama,
                onUbah = { barisUtama = it }
            )

            BagianHasilProduksi(
                utama = false,
                daftar = barisSamping,
                onUbah = { barisSamping = it }
            )

            if (input != null && inputError == null &&
                semuaBarisLengkap && adaHasil
            ) {
                val susut = input - totalHasil

                if (susut.signum() < 0) {
                    Text(
                        text = "Total hasil ${formatProduksi(totalHasil)} kg " +
                                "melebihi bahan yang dipakai ${formatProduksi(input)} kg.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    AccessCard {
                        Text(
                            "Ringkasan hasil",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Hasil utama: ${formatProduksi(rincian.totalUtamaKg)} kg " +
                                    "(${persenProduksi(rincian.totalUtamaKg, input)}%)"
                        )
                        Text(
                            "Hasil samping: ${formatProduksi(rincian.totalSampingKg)} kg " +
                                    "(${persenProduksi(rincian.totalSampingKg, input)}%)"
                        )
                        HorizontalDivider(color = AccessColors.Border)
                        Text(
                            "Susut: ${formatProduksi(susut)} kg " +
                                    "(${persenProduksi(susut, input)}%)",
                            color = AccessColors.Green,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Button(
                enabled = input != null && inputError == null &&
                        semuaBarisLengkap && adaHasil && totalHasil <= input,
                onClick = {
                    val beratBahan = input ?: return@Button
                    val tanggal = SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.ROOT
                    ).format(Date())

                    onKonfirmasi(
                        BatchProduksiUi(
                            kode = "",
                            tanggal = tanggal,
                            kodeBahan = sumber.kode,
                            beratInput = beratBahan,
                            rincian = rincian
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccessColors.Green
                )
            ) {
                Text("Konfirmasi produksi")
            }
        }
    }
}

@Composable
private fun BagianHasilProduksi(
    utama: Boolean,
    daftar: List<BarisHasilForm>,
    onUbah: (ArrayList<BarisHasilForm>) -> Unit
) {
    val jenis = if (utama) "hasil utama" else "hasil samping"
    val produkTersedia = if (utama) {
        kemasanPerProduk.keys.toList()
    } else {
        daftarHasilSamping
    }

    Text(
        text = if (utama) "Hasil utama" else "Hasil samping",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = AccessColors.Ink
    )

    daftar.forEachIndexed { indeks, baris ->
        key(baris.id) {
            fun perbarui(baru: BarisHasilForm) {
                val salinan = ArrayList(daftar)
                salinan[indeks] = baru
                onUbah(salinan)
            }

            AccessCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = if (utama) "Hasil utama ${indeks + 1}"
                        else "Hasil samping ${indeks + 1}",
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(
                        onClick = {
                            val salinan = ArrayList(daftar)
                            salinan.removeAt(indeks)
                            onUbah(salinan)
                        }
                    ) {
                        Text("Hapus")
                    }
                }

                PilihanProduksi(
                    label = if (utama) "Produk utama" else "Produk hasil samping",
                    petunjuk = "Pilih produk",
                    nilai = baris.produk,
                    pilihan = produkTersedia,
                    onPilih = { produk ->
                        if (produk != baris.produk) {
                            perbarui(baris.copy(produk = produk, kemasan = ""))
                        }
                    }
                )

                if (utama) {
                    PilihanProduksi(
                        label = "Kemasan",
                        petunjuk = if (baris.produk.isEmpty()) {
                            "Pilih produk dahulu"
                        } else {
                            "Pilih kemasan"
                        },
                        nilai = baris.kemasan,
                        pilihan = kemasanPerProduk[baris.produk].orEmpty(),
                        onPilih = { perbarui(baris.copy(kemasan = it)) }
                    )
                }

                val berat = beratProduksi(baris.berat)
                val pesanError = if (
                    baris.berat.isNotBlank() &&
                    (berat == null || berat.signum() <= 0)
                ) {
                    "Masukkan berat lebih dari 0 kg."
                } else {
                    null
                }

                KolomBeratProduksi(
                    label = "Berat hasil (kg)",
                    nilai = baris.berat,
                    ubah = { perbarui(baris.copy(berat = it)) },
                    pesanError = pesanError
                )
            }
        }
    }

    OutlinedButton(
        onClick = {
            val salinan = ArrayList(daftar)
            salinan.add(BarisHasilForm())
            onUbah(salinan)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("+ Tambah $jenis")
    }
}
@Composable
private fun PilihanProduksi(
    label: String,
    petunjuk: String,
    nilai: String,
    pilihan: List<String>,
    teksPilihan: (String) -> String = { it },
    onPilih: (String) -> Unit
) {
    var terbuka by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = AccessColors.Ink
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { terbuka = true },
                enabled = pilihan.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (nilai.isBlank()) petunjuk else teksPilihan(nilai),
                    modifier = Modifier.weight(1f)
                )
                Text("▾")
            }

            DropdownMenu(
                expanded = terbuka && pilihan.isNotEmpty(),
                onDismissRequest = { terbuka = false },
                modifier = Modifier.heightIn(max = 320.dp)
            ) {
                pilihan.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(teksPilihan(item)) },
                        onClick = {
                            terbuka = false
                            onPilih(item)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KolomBeratProduksi(
    label: String,
    nilai: String,
    ubah: (String) -> Unit,
    pesanError: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = nilai,
            onValueChange = ubah,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = pesanError != null,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        if (pesanError != null) {
            Text(
                text = pesanError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
    }
}



private fun beratProduksi(teks: String): BigDecimal? {
    val angka = teks.trim()
    if (!Regex("[0-9]+([.,][0-9]+)?").matches(angka)) return null
    return angka.replace(',', '.').toBigDecimalOrNull()
}

private fun formatProduksi(nilai: BigDecimal): String {
    return NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 3
    }.format(nilai)
}

private fun persenProduksi(bagian: BigDecimal, total: BigDecimal): String {
    val persen = bagian
        .multiply(BigDecimal.valueOf(100))
        .divide(total, 2, RoundingMode.HALF_UP)

    return formatProduksi(persen)
}
@androidx.compose.ui.tooling.preview.Preview(
    name = "Hasil Produksi · Form terisi",
    showBackground = true,
    widthDp = 390,
    heightDp = 2400
)
@Composable
private fun HitungProduksiTerisiPreview() {
    val contohLot = contohBahanBaku().first()

    com.stb.sirabas.ui.theme.SIRABASTheme {
        FormHitungProduksi(
            bahanTersedia = listOf(contohLot),
            onKembali = {},
            onKonfirmasi = {},
            kodeAwal = contohLot.kode,
            beratAwal = "200",
            utamaAwal = listOf(
                BarisHasilForm(
                    produk = "Premium",
                    kemasan = "5 kg",
                    berat = "180"
                )
            ),
            sampingAwal = listOf(
                BarisHasilForm(produk = "Dedak Halus", berat = "8"),
                BarisHasilForm(produk = "Sekam", berat = "5")
            )
        )
    }
}