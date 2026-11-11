package com.stb.sirabas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme

data class PilihanKemasanUi(
    val kodeBatch: String,
    val tanggal: String,
    val namaProduk: String,
    val kemasan: String,
    val jumlahKarung: Int
)

@Composable
fun PilihKemasanScreen(
    daftarKemasan: List<PilihanKemasanUi>,
    onKembali: () -> Unit,
    onPilih: (PilihanKemasanUi) -> Unit
) {
    val kemasanPerBatch = daftarKemasan.groupBy { it.kodeBatch }

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
                    text = "Label hasil kemasan",
                    color = AccessColors.Ink,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pilih kemasan untuk melihat label karung.",
                    color = AccessColors.Muted,
                    fontSize = 14.sp
                )
            }
        }

        if (kemasanPerBatch.isEmpty()) {
            item {
                AccessCard {
                    Text(
                        text = "Belum ada hasil kemasan yang tercatat.",
                        color = AccessColors.Muted
                    )
                }
            }
        }

        kemasanPerBatch.forEach { (kodeBatch, daftarProduk) ->
            item {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Batch $kodeBatch",
                        color = AccessColors.Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = daftarProduk.first().tanggal,
                        color = AccessColors.Muted,
                        fontSize = 13.sp
                    )
                }
            }

            items(daftarProduk.size) { indeks ->
                val produk = daftarProduk[indeks]

                Surface(
                    onClick = { onPilih(produk) },
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
                                text = "${produk.namaProduk} · ${produk.kemasan}",
                                color = AccessColors.Ink,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "${produk.jumlahKarung} karung",
                                color = AccessColors.Muted,
                                fontSize = 14.sp
                            )
                        }

                        Text("›", color = AccessColors.Green, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Label & Cetak · Pilih kemasan",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PilihKemasanPreview() {
    SIRABASTheme {
        PilihKemasanScreen(
            daftarKemasan = listOf(
                PilihanKemasanUi(
                    kodeBatch = "PR/001",
                    tanggal = "11 November 2026",
                    namaProduk = "Premium",
                    kemasan = "5 kg",
                    jumlahKarung = 36
                ),
                PilihanKemasanUi(
                    kodeBatch = "PR/001",
                    tanggal = "11 November 2026",
                    namaProduk = "Premium",
                    kemasan = "25 kg hijau",
                    jumlahKarung = 3
                )
            ),
            onKembali = {},
            onPilih = {}
        )
    }
}