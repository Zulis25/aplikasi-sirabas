package com.stb.sirabas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
fun PilihLotLabelScreen(
    daftarBahan: List<BahanBakuUi>,
    onKembali: () -> Unit,
    onPilihLot: (BahanBakuUi) -> Unit
) {
    val formatBerat = remember {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    }

    // Tanggal penerimaan pada model menggunakan format yyyy-MM-dd.
    val lotTerbaruDahulu = daftarBahan.sortedByDescending { it.tanggal }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onKembali) {
                Text("‹ Kembali ke Label & Cetak", color = AccessColors.Green)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Pilih lot bahan masuk",
                    color = AccessColors.Ink,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pilih penerimaan untuk melihat label lot.",
                    color = AccessColors.Muted,
                    fontSize = 14.sp
                )
            }
        }

        if (lotTerbaruDahulu.isEmpty()) {
            item {
                AccessCard {
                    Text(
                        text = "Belum ada penerimaan bahan baku.",
                        color = AccessColors.Muted
                    )
                }
            }
        }

        items(
            items = lotTerbaruDahulu,
            key = { bahan -> bahan.kode }
        ) { bahan ->
            Surface(
                onClick = { onPilihLot(bahan) },
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AccessColors.Border)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "${bahan.kode} · ${bahan.jenis}",
                            color = AccessColors.Ink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${bahan.supplier} · ${bahan.asalDaerah}",
                            color = AccessColors.Ink,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Diterima ${bahan.tanggal}",
                            color = AccessColors.Muted,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Berat diterima: ${formatBerat.format(bahan.beratKg)} kg",
                            color = AccessColors.Green,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Text("›", color = AccessColors.Green, fontSize = 24.sp)
                }
            }
        }
    }
}

@Preview(
    name = "Label & Cetak · Pilih lot bahan masuk",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PilihLotLabelPreview() {
    SIRABASTheme {
        PilihLotLabelScreen(
            daftarBahan = contohBahanBaku(),
            onKembali = {},
            onPilihLot = {}
        )
    }
}