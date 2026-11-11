package com.stb.sirabas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.math.BigInteger
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class PeriodePenjualan(val judul: String) {
    BULAN_INI("Bulan ini"),
    TIGA_BULAN("3 bulan terakhir"),
    TAHUN_INI("Tahun ini"),
    RENTANG("Rentang tanggal")
}

private fun formatTanggalLaporan(pola: String): SimpleDateFormat {
    return SimpleDateFormat(pola, Locale.forLanguageTag("id-ID")).apply {
        isLenient = false
    }
}

private fun bacaTanggalInput(teks: String): Date? {
    if (!Regex("[0-9]{2}/[0-9]{2}/[0-9]{4}").matches(teks)) return null
    return runCatching { formatTanggalLaporan("dd/MM/yyyy").parse(teks) }
        .getOrNull()
}

@Composable
fun LaporanPenjualanScreen(
    daftarInvoice: List<InvoicePenjualanUi>,
    onUnduhExcel: (
        tanggalMulai: Date,
        tanggalAkhir: Date,
        invoiceTerpilih: List<InvoicePenjualanUi>
    ) -> Unit,
    tanggalAcuan: Date = Date()
) {
    val formatInput = remember { formatTanggalLaporan("dd/MM/yyyy") }
    val formatPanjang = remember { formatTanggalLaporan("d MMMM yyyy") }
    val formatRupiah = remember {
        NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"))
    }

    // Semua perbandingan memakai tanggal tanpa jam.
    val hariIni = requireNotNull(formatInput.parse(formatInput.format(tanggalAcuan)))
    val kalender = Calendar.getInstance().apply {
        time = hariIni
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val awalBulan = kalender.time

    var namaPeriode by rememberSaveable {
        mutableStateOf(PeriodePenjualan.BULAN_INI.name)
    }
    var mulaiInput by rememberSaveable {
        mutableStateOf(formatInput.format(awalBulan))
    }
    var akhirInput by rememberSaveable {
        mutableStateOf(formatInput.format(hariIni))
    }
    var menuTerbuka by remember { mutableStateOf(false) }

    val periode = PeriodePenjualan.valueOf(namaPeriode)

    val mulai: Date? = when (periode) {
        PeriodePenjualan.BULAN_INI -> awalBulan
        PeriodePenjualan.TIGA_BULAN -> {
            kalender.add(Calendar.MONTH, -2)
            kalender.time
        }
        PeriodePenjualan.TAHUN_INI -> {
            kalender.set(Calendar.MONTH, Calendar.JANUARY)
            kalender.time
        }
        PeriodePenjualan.RENTANG -> bacaTanggalInput(mulaiInput)
    }

    val akhir: Date? = if (periode == PeriodePenjualan.RENTANG) {
        bacaTanggalInput(akhirInput)
    } else {
        hariIni
    }

    val periodeValid = mulai != null && akhir != null && !mulai.after(akhir)

    // InvoicePenjualanUi yang sudah kamu buat menyimpan tanggal
    // dalam bentuk teks, misalnya "11 November 2026".
    val invoicePeriode = if (mulai != null && akhir != null && periodeValid) {
        daftarInvoice.mapNotNull { invoice ->
            val tanggal = runCatching {
                formatPanjang.parse(invoice.tanggal)
            }.getOrNull()

            if (tanggal != null && !tanggal.before(mulai) && !tanggal.after(akhir)) {
                tanggal to invoice
            } else {
                null
            }
        }.sortedByDescending { it.first }.map { it.second }
    } else {
        emptyList()
    }

    val totalPenjualan = invoicePeriode.fold(BigInteger.ZERO) { total, invoice ->
        total + invoice.total
    }
    val totalDibayar = invoicePeriode.fold(BigInteger.ZERO) { total, invoice ->
        total + invoice.jumlahDibayar.toBigInteger()
    }
    val totalPiutang = invoicePeriode.fold(BigInteger.ZERO) { total, invoice ->
        total + invoice.sisaPiutang
    }

    fun rupiah(nilai: BigInteger): String = "Rp${formatRupiah.format(nilai)}"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Laporan penjualan",
                color = AccessColors.Ink,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Periode laporan",
                    color = AccessColors.Ink,
                    fontWeight = FontWeight.SemiBold
                )

                Box {
                    OutlinedButton(
                        onClick = { menuTerbuka = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(periode.judul, color = AccessColors.Ink)
                            Text("▾", color = AccessColors.Green)
                        }
                    }

                    DropdownMenu(
                        expanded = menuTerbuka,
                        onDismissRequest = { menuTerbuka = false }
                    ) {
                        PeriodePenjualan.values().forEach { pilihan ->
                            DropdownMenuItem(
                                text = { Text(pilihan.judul) },
                                onClick = {
                                    namaPeriode = pilihan.name
                                    menuTerbuka = false
                                }
                            )
                        }
                    }
                }

                if (periode == PeriodePenjualan.RENTANG) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mulaiInput,
                            onValueChange = { mulaiInput = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Dari") },
                            placeholder = { Text("dd/MM/yyyy") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text
                            ),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = akhirInput,
                            onValueChange = { akhirInput = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Sampai") },
                            placeholder = { Text("dd/MM/yyyy") },
                            singleLine = true
                        )
                    }
                }

                if (mulai != null && akhir != null && periodeValid) {
                    Text(
                        text = "${formatPanjang.format(mulai)} – ${formatPanjang.format(akhir)}",
                        color = AccessColors.Muted,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = "Gunakan dd/MM/yyyy. Tanggal akhir harus sama atau sesudah tanggal mulai.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }
            }
        }

        if (periodeValid) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF123F2E),
                    contentColor = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Total penjualan", fontSize = 14.sp)
                        Text(
                            text = rupiah(totalPenjualan),
                            fontSize = 29.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${invoicePeriode.size} invoice",
                            color = Color(0xFFE3CB91)
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BarisRingkasanPenjualan("Sudah dibayar", rupiah(totalDibayar))
                        HorizontalDivider(color = AccessColors.Border)
                        BarisRingkasanPenjualan("Sisa piutang", rupiah(totalPiutang))
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (mulai != null && akhir != null) {
                            onUnduhExcel(mulai, akhir, invoicePeriode)
                        }
                    },
                    enabled = invoicePeriode.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccessColors.Green,
                        contentColor = Color.White
                    )
                ) {
                    Text("Unduh laporan Excel")
                }
            }

            item {
                Text(
                    text = "Rincian invoice",
                    color = AccessColors.Ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (invoicePeriode.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada penjualan pada periode ini.",
                        color = AccessColors.Muted
                    )
                }
            }

            items(invoicePeriode, key = { it.nomor }) { invoice ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        BarisRingkasanPenjualan(
                            invoice.pelanggan,
                            rupiah(invoice.total)
                        )
                        Text(
                            text = "${invoice.nomor} · ${invoice.tanggal}",
                            color = AccessColors.Muted,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (invoice.lunas) {
                                "Lunas"
                            } else {
                                "Piutang · tersisa ${rupiah(invoice.sisaPiutang)}"
                            },
                            color = if (invoice.lunas) {
                                AccessColors.Green
                            } else {
                                Color(0xFF815300)
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarisRingkasanPenjualan(judul: String, nilai: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = judul,
            modifier = Modifier.weight(1f),
            color = AccessColors.Ink
        )
        Text(
            text = nilai,
            color = AccessColors.Ink,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(
    name = "Petugas Penjualan · Laporan",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun LaporanPenjualanPreview() {
    val beras = ProdukPenjualanUi(
        kode = "PREMIUM-25-HIJAU",
        nama = "Beras Premium",
        kemasan = "25 kg hijau",
        satuanJual = "karung",
        stokTersedia = 12,
        hargaKatalog = 350_000L
    )

    val invoice = listOf(
        InvoicePenjualanUi(
            nomor = "INV/2026/002",
            tanggal = "11 November 2026",
            pelanggan = "MBG B",
            daftarItem = listOf(ItemPesananUi(beras, 2, 350_000L)),
            jumlahDibayar = 200_000L,
            metodePembayaran = "Cash"
        ),
        InvoicePenjualanUi(
            nomor = "INV/2026/001",
            tanggal = "10 November 2026",
            pelanggan = "MBG A",
            daftarItem = listOf(ItemPesananUi(beras, 3, 350_000L)),
            jumlahDibayar = 1_050_000L,
            metodePembayaran = "Transfer Bank"
        )
    )

    SIRABASTheme {
        LaporanPenjualanScreen(
            daftarInvoice = invoice,
            tanggalAcuan = requireNotNull(bacaTanggalInput("11/11/2026")),
            onUnduhExcel = { _, _, _ -> }
        )
    }
}