package com.stb.sirabas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProduksiTersimpanScreen(
    batch: BatchProduksiUi,
    onSiapkanLabel: (BatchProduksiUi) -> Unit,
    onKembaliKeProduksi: () -> Unit
) {
    val formatBerat = remember {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).apply {
            maximumFractionDigits = 3
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Produksi tersimpan",
            color = AccessColors.Ink,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF123F2E),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Kode batch produksi",
                    color = Color(0xFFE3CB91),
                    fontSize = 14.sp
                )
                Text(
                    text = batch.kode,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Lot bahan asal: ${batch.kodeBahan}", color = Color.White)
                Text("Tanggal produksi: ${batch.tanggal}", color = Color.White)
            }
        }

        AccessCard {
            Text(
                text = "Hasil penggilingan",
                color = AccessColors.Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text("Bahan yang dipakai: ${formatBerat.format(batch.beratInput)} kg")

            if (batch.rincian.hasilUtama.isNotEmpty()) {
                HorizontalDivider(color = AccessColors.Border)
                Text(
                    text = "Hasil utama",
                    color = AccessColors.Ink,
                    fontWeight = FontWeight.SemiBold
                )

                batch.rincian.hasilUtama.forEach { hasil ->
                    Text(
                        "${hasil.namaProduk} · ${hasil.kemasan} — " +
                                "${formatBerat.format(hasil.beratKg)} kg"
                    )
                }
            }

            if (batch.rincian.hasilSamping.isNotEmpty()) {
                HorizontalDivider(color = AccessColors.Border)
                Text(
                    text = "Hasil samping",
                    color = AccessColors.Ink,
                    fontWeight = FontWeight.SemiBold
                )

                batch.rincian.hasilSamping.forEach { hasil ->
                    Text("${hasil.namaProduk} — ${formatBerat.format(hasil.beratKg)} kg")
                }
            }

            HorizontalDivider(color = AccessColors.Border)
            Text(
                text = "Susut: ${formatBerat.format(batch.susut)} kg",
                color = AccessColors.Green,
                fontWeight = FontWeight.SemiBold
            )
        }

        Button(
            onClick = { onSiapkanLabel(batch) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccessColors.Green,
                contentColor = Color.White
            )
        ) {
            Text("Siapkan label karung")
        }

        OutlinedButton(
            onClick = onKembaliKeProduksi,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Kembali ke menu Produksi", color = AccessColors.Green)
        }
    }
}

@Preview(
    name = "Hasil produksi · Produksi tersimpan",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun ProduksiTersimpanPreview() {
    val contohBatch = BatchProduksiUi(
        kode = "PR/001",
        tanggal = "2026-11-11",
        kodeBahan = "BM/024",
        beratInput = BigDecimal("200"),
        rincian = RincianHasilProduksiUi(
            hasilUtama = listOf(
                HasilUtamaProduksiUi(
                    namaProduk = "Premium",
                    kemasan = "5 kg",
                    beratKg = BigDecimal("180")
                )
            ),
            hasilSamping = listOf(
                HasilSampingProduksiUi("Dedak Halus", BigDecimal("8")),
                HasilSampingProduksiUi("Sekam", BigDecimal("5"))
            )
        )
    )

    SIRABASTheme {
        ProduksiTersimpanScreen(
            batch = contohBatch,
            onSiapkanLabel = {},
            onKembaliKeProduksi = {}
        )
    }
}