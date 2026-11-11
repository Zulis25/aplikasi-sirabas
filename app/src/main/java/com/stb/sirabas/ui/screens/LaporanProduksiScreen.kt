package com.stb.sirabas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class PermintaanLaporanProduksiUi(
    val jenis: String,
    val tanggalMulai: String,
    val tanggalAkhir: String
)

private val jenisLaporanProduksi = listOf(
    "Bahan baku masuk",
    "Hasil produksi",
    "Stok gudang"
)

private val periodeLaporanProduksi = listOf(
    "Bulanan",
    "3 bulan",
    "Tahunan",
    "Rentang tanggal"
)

@Composable
fun LaporanProduksiScreen(
    onUnduhExcel: (PermintaanLaporanProduksiUi) -> Unit,
    tanggalAcuan: Date = Date()
) {
    val kalender = Calendar.getInstance().apply { time = tanggalAcuan }
    val tahunAcuan = kalender.get(Calendar.YEAR)
    val bulanAcuan = kalender.get(Calendar.MONTH)

    var jenis by rememberSaveable { mutableStateOf("Hasil produksi") }
    var periode by rememberSaveable { mutableStateOf("Bulanan") }
    var tahunTeks by rememberSaveable { mutableStateOf(tahunAcuan.toString()) }
    var bulan by rememberSaveable { mutableStateOf(bulanAcuan) }
    var mulaiManual by rememberSaveable { mutableStateOf("") }
    var akhirManual by rememberSaveable { mutableStateOf("") }

    val namaBulan = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    val tahun = tahunTeks.toIntOrNull()
    val rentang = when {
        periode == "Rentang tanggal" -> {
            val mulai = tanggalLaporanValid(mulaiManual)
            val akhir = tanggalLaporanValid(akhirManual)
            if (mulai != null && akhir != null && mulai <= akhir) {
                mulai to akhir
            } else {
                null
            }
        }

        tahun != null && tahunTeks.length == 4 && tahun > 0 ->
            rentangKalenderProduksi(periode, tahun, bulan)

        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Laporan produksi",
            color = AccessColors.Ink,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        PilihanLaporan(
            label = "Jenis laporan",
            nilai = jenis,
            pilihan = jenisLaporanProduksi,
            onPilih = { jenis = it }
        )

        PilihanLaporan(
            label = "Periode",
            nilai = periode,
            pilihan = periodeLaporanProduksi,
            onPilih = { periode = it }
        )

        if (periode == "Rentang tanggal") {
            KolomTanggalLaporan(
                label = "Tanggal mulai",
                nilai = mulaiManual,
                onUbah = { mulaiManual = it }
            )
            KolomTanggalLaporan(
                label = "Tanggal akhir",
                nilai = akhirManual,
                onUbah = { akhirManual = it }
            )

            if (tanggalLaporanValid(mulaiManual) != null &&
                tanggalLaporanValid(akhirManual) != null &&
                akhirManual < mulaiManual
            ) {
                Text(
                    "Tanggal akhir harus sama dengan atau setelah tanggal mulai.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            if (periode != "Tahunan") {
                PilihanLaporan(
                    label = if (periode == "3 bulan") "Bulan awal" else "Bulan",
                    nilai = namaBulan[bulan],
                    pilihan = namaBulan,
                    onPilih = { bulan = namaBulan.indexOf(it) }
                )
            }

            OutlinedTextField(
                value = tahunTeks,
                onValueChange = { tahunTeks = it },
                label = { Text("Tahun") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (rentang != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF123F2E)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Ringkasan unduhan", color = Color(0xFFE3CB91))
                    Text(
                        text = jenis,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${tanggalLaporanTampil(rentang.first)} – " +
                                tanggalLaporanTampil(rentang.second),
                        color = Color.White
                    )
                }
            }
        }

        Button(
            onClick = {
                if (rentang != null) {
                    onUnduhExcel(
                        PermintaanLaporanProduksiUi(
                            jenis = jenis,
                            tanggalMulai = rentang.first,
                            tanggalAkhir = rentang.second
                        )
                    )
                }
            },
            enabled = rentang != null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccessColors.Green,
                contentColor = Color.White
            )
        ) {
            Text("Unduh Excel")
        }
    }
}

@Composable
private fun PilihanLaporan(
    label: String,
    nilai: String,
    pilihan: List<String>,
    onPilih: (String) -> Unit
) {
    var terbuka by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = AccessColors.Ink, fontWeight = FontWeight.SemiBold)

        Box {
            OutlinedButton(
                onClick = { terbuka = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(nilai, modifier = Modifier.weight(1f))
                Text("▾")
            }

            DropdownMenu(
                expanded = terbuka,
                onDismissRequest = { terbuka = false }
            ) {
                pilihan.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onPilih(item)
                            terbuka = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KolomTanggalLaporan(
    label: String,
    nilai: String,
    onUbah: (String) -> Unit
) {
    val salah = nilai.isNotBlank() && tanggalLaporanValid(nilai) == null

    OutlinedTextField(
        value = nilai,
        onValueChange = onUbah,
        label = { Text(label) },
        placeholder = { Text("yyyy-MM-dd") },
        singleLine = true,
        isError = salah,
        supportingText = {
            if (salah) Text("Gunakan tanggal yang valid, misalnya 2026-11-01.")
        },
        modifier = Modifier.fillMaxWidth()
    )
}

private fun tanggalLaporanValid(teks: String): String? {
    if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(teks)) return null

    val format = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        isLenient = false
    }
    return runCatching { format.parse(teks) }.getOrNull()?.let { teks }
}

private fun tanggalLaporanTampil(teks: String): String {
    val tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(teks)
        ?: return teks
    return SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID"))
        .format(tanggal)
}

private fun rentangKalenderProduksi(
    periode: String,
    tahun: Int,
    bulan: Int
): Pair<String, String> {
    val awal = Calendar.getInstance().apply {
        clear()
        set(tahun, if (periode == "Tahunan") Calendar.JANUARY else bulan, 1)
    }

    val akhir = (awal.clone() as Calendar).apply {
        val jumlahBulan = when (periode) {
            "Tahunan" -> 12
            "3 bulan" -> 3
            else -> 1
        }
        add(Calendar.MONTH, jumlahBulan)
        add(Calendar.DAY_OF_MONTH, -1)
    }

    val format = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    return format.format(awal.time) to format.format(akhir.time)
}

@Preview(
    name = "Petugas produksi · Laporan",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun LaporanProduksiPreview() {
    val contohTanggal = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, 11)
    }.time

    SIRABASTheme {
        LaporanProduksiScreen(
            onUnduhExcel = {},
            tanggalAcuan = contohTanggal
        )
    }
}