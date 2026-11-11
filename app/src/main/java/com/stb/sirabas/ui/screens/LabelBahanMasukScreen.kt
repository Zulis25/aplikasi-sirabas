package com.stb.sirabas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LabelBahanMasukScreen(
    bahan: BahanBakuUi,
    onKembali: () -> Unit,
    onCetak: (kodeLot: String, jumlahSalinan: Int) -> Unit
) {
    var jumlahSalinan by rememberSaveable(bahan.kode) {
        mutableStateOf(1)
    }

    val formatBerat = remember {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onKembali) {
            Text("‹ Kembali ke Label & Cetak", color = AccessColors.Green)
        }

        Text(
            text = "Label bahan masuk",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AccessColors.Ink
        )

        Text(
            text = "Pratinjau label",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = AccessColors.Ink
        )

        // Informasi di dalam kartu ini adalah isi label fisik.
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AccessColors.Border)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "PT. Sri Tani Berkah",
                    color = AccessColors.Green,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${bahan.jenis} · ${bahan.kode}",
                    color = AccessColors.Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )

                HorizontalDivider(color = AccessColors.Border)

                DetailLabelLot("Supplier", bahan.supplier)
                DetailLabelLot("Asal bahan", bahan.asalDaerah)
                DetailLabelLot("Tanggal diterima", bahan.tanggal)
                DetailLabelLot(
                    "Berat saat diterima",
                    "${formatBerat.format(bahan.beratKg)} kg"
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Jumlah salinan",
                color = AccessColors.Ink,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { jumlahSalinan-- },
                    enabled = jumlahSalinan > 1,
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("−")
                }

                Text(
                    text = jumlahSalinan.toString(),
                    fontWeight = FontWeight.Bold,
                    color = AccessColors.Ink
                )

                OutlinedButton(
                    onClick = { jumlahSalinan++ },
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("+")
                }
            }
        }

        Button(
            onClick = { onCetak(bahan.kode, jumlahSalinan) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccessColors.Green
            )
        ) {
            Text("Cetak label lot")
        }
    }
}

@Composable
private fun DetailLabelLot(judul: String, nilai: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = judul,
            fontSize = 12.sp,
            color = AccessColors.Muted
        )
        Text(
            text = nilai,
            fontSize = 16.sp,
            color = AccessColors.Ink,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(
    name = "Produksi · Label bahan masuk",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun LabelBahanMasukPreview() {
    SIRABASTheme {
        LabelBahanMasukScreen(
            bahan = contohBahanBaku().first(),
            onKembali = {},
            onCetak = { _, _ -> }
        )
    }
}