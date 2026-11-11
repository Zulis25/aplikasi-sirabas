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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FormBahanBakuScreen(
    kodeBaru: String,
    onKembali: () -> Unit,
    onSimpan: (BahanBakuUi) -> Unit
) {
    var jenis by rememberSaveable { mutableStateOf("Gabah") }
    var supplier by rememberSaveable { mutableStateOf("") }
    var daerah by rememberSaveable { mutableStateOf("") }
    var berat by rememberSaveable { mutableStateOf("") }

    var tanggal by rememberSaveable {
        mutableStateOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
        )
    }

    var air by rememberSaveable { mutableStateOf("") }
    var sosoh by rememberSaveable { mutableStateOf("") }
    var broken by rememberSaveable { mutableStateOf("") }
    var periksa by rememberSaveable { mutableStateOf(false) }

    // Menerima angka desimal dengan koma maupun titik.
    val beratAngka = bacaAngkaBahan(berat)
    val airAngka = bacaAngkaBahan(air)
    val sosohAngka = bacaAngkaBahan(sosoh)
    val brokenAngka = bacaAngkaBahan(broken)

    val salahBerat = beratAngka == null || beratAngka <= 0.0
    val salahTanggal = !tanggalBahanValid(tanggal)

    fun persenSalah(isi: String, wajib: Boolean): Boolean {
        if (isi.isBlank()) return wajib
        val nilai = bacaAngkaBahan(isi) ?: return true
        return nilai !in 0.0..100.0
    }

    val salahAir = persenSalah(air, wajib = true)
    val salahSosoh = persenSalah(sosoh, wajib = jenis == "Beras")
    val salahBroken = persenSalah(broken, wajib = jenis == "Beras")

    val valid = supplier.isNotBlank() &&
            daerah.isNotBlank() &&
            !salahBerat &&
            !salahTanggal &&
            !salahAir &&
            !salahSosoh &&
            !salahBroken

    BackHandler(onBack = onKembali)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TextButton(onClick = onKembali) {
                Text("← Kembali ke daftar", color = AccessColors.Green)
            }

            Text(
                text = "Tambah bahan masuk",
                color = AccessColors.Ink,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Nomor penerimaan: $kodeBaru",
                color = AccessColors.Green,
                fontWeight = FontWeight.SemiBold
            )

            Text("Jenis bahan baku", color = AccessColors.Ink)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Gabah", "Beras").forEach { pilihan ->
                    FilterChip(
                        selected = jenis == pilihan,
                        onClick = { jenis = pilihan },
                        label = { Text(pilihan) }
                    )
                }
            }

            KolomPenerimaan(
                judul = "Nama pemasok",
                nilai = supplier,
                ubah = { supplier = it },
                error = if (periksa && supplier.isBlank()) {
                    "Masukkan nama pemasok."
                } else null
            )

            KolomPenerimaan(
                judul = "Asal daerah",
                nilai = daerah,
                ubah = { daerah = it },
                error = if (periksa && daerah.isBlank()) {
                    "Masukkan asal daerah bahan baku."
                } else null
            )

            KolomPenerimaan(
                judul = "Tanggal masuk",
                nilai = tanggal,
                ubah = { tanggal = it },
                petunjuk = "Format: tahun-bulan-tanggal, misalnya 2026-11-11.",
                error = if (periksa && salahTanggal) {
                    "Masukkan tanggal yang valid dengan format yyyy-MM-dd."
                } else null
            )

            KolomPenerimaan(
                judul = "Berat masuk (kg)",
                nilai = berat,
                ubah = { berat = it },
                angka = true,
                petunjuk = "Contoh: 500 atau 500,5. Tanpa pemisah ribuan.",
                error = if (periksa && salahBerat) {
                    "Berat harus berupa angka lebih dari 0 kg."
                } else null
            )

            HorizontalDivider(color = AccessColors.Border)

            Text(
                text = "Pengujian bahan baku",
                color = AccessColors.Ink,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            KolomPenerimaan(
                judul = "Kadar air (%)",
                nilai = air,
                ubah = { air = it },
                angka = true,
                error = if (periksa && salahAir) {
                    "Masukkan kadar air antara 0 dan 100%."
                } else null
            )

            KolomPenerimaan(
                judul = "Derajat sosoh (%)",
                nilai = sosoh,
                ubah = { sosoh = it },
                angka = true,
                petunjuk = if (jenis == "Gabah") {
                    "Kosongkan jika belum diuji."
                } else null,
                error = if (periksa && salahSosoh) {
                    "Masukkan derajat sosoh antara 0 dan 100%."
                } else null
            )

            KolomPenerimaan(
                judul = "Broken (%)",
                nilai = broken,
                ubah = { broken = it },
                angka = true,
                petunjuk = if (jenis == "Gabah") {
                    "Kosongkan jika belum diuji."
                } else null,
                error = if (periksa && salahBroken) {
                    "Masukkan persentase broken antara 0 dan 100%."
                } else null
            )

            Button(
                onClick = {
                    periksa = true

                    if (valid && beratAngka != null) {
                        onSimpan(
                            BahanBakuUi(
                                kode = kodeBaru,
                                tanggal = tanggal.trim(),
                                jenis = jenis,
                                supplier = supplier.trim(),
                                asalDaerah = daerah.trim(),
                                beratKg = beratAngka,
                                kadarAir = airAngka,
                                derajatSosoh = sosohAngka,
                                broken = brokenAngka
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccessColors.Green,
                    contentColor = Color.White
                )
            ) {
                Text("Simpan penerimaan", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun KolomPenerimaan(
    judul: String,
    nilai: String,
    ubah: (String) -> Unit,
    angka: Boolean = false,
    petunjuk: String? = null,
    error: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = nilai,
            onValueChange = ubah,
            label = { Text(judul) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (angka) {
                    KeyboardType.Decimal
                } else {
                    KeyboardType.Text
                }
            )
        )

        val keterangan = error ?: petunjuk
        if (keterangan != null) {
            Text(
                text = keterangan,
                fontSize = 12.sp,
                color = if (error != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    AccessColors.Muted
                }
            )
        }
    }
}

private fun bacaAngkaBahan(teks: String): Double? {
    val bersih = teks.trim()
    if (!Regex("[0-9]+([.,][0-9]+)?").matches(bersih)) return null
    return bersih.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
}

private fun tanggalBahanValid(teks: String): Boolean {
    val bersih = teks.trim()
    if (!Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(bersih)) return false

    val format = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        isLenient = false
    }
    return runCatching { format.parse(bersih) != null }.getOrDefault(false)
}
@androidx.compose.ui.tooling.preview.Preview(
    name = "Produksi · Penerimaan Bahan Baku",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun FormBahanBakuPreview() {
    com.stb.sirabas.ui.theme.SIRABASTheme {
        FormBahanBakuScreen(
            kodeBaru = "BM/026",
            onKembali = {},
            onSimpan = {}
        )
    }
}