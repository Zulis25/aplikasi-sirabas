package com.stb.sirabas.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

// Satu pilihan mewakili satu varian kemasan dalam batch.
data class KemasanUntukLabelUi(
    val namaProduk: String,
    val kemasan: String,
    val beratPerKarungKg: BigDecimal,
    val beratBelumDilabeliKg: BigDecimal
)

@Composable
fun SiapkanLabelKarungScreen(
    kodeBatch: String,
    kodeLot: String,
    daftarKemasan: List<KemasanUntukLabelUi>,
    onKembali: () -> Unit,
    onLihatLabel: (KemasanUntukLabelUi, Int) -> Unit,
    jumlahAwal: String = ""
) {
    var indeksTerpilih by rememberSaveable(kodeBatch) {
        mutableStateOf(0)
    }
    var menuTerbuka by remember { mutableStateOf(false) }

    val pilihan = daftarKemasan.getOrNull(indeksTerpilih)
    var jumlahTeks by rememberSaveable(kodeBatch, pilihan?.namaProduk, pilihan?.kemasan) {
        mutableStateOf(jumlahAwal)
    }

    val formatAngka = remember {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    }

    val jumlah = jumlahTeks.toIntOrNull()
    val maksimumKarung = pilihan?.let {
        if (it.beratPerKarungKg.signum() > 0) {
            it.beratBelumDilabeliKg.divideToIntegralValue(it.beratPerKarungKg)
        } else {
            BigDecimal.ZERO
        }
    } ?: BigDecimal.ZERO

    val jumlahValid = jumlah != null &&
            jumlah > 0 &&
            BigDecimal.valueOf(jumlah.toLong()) <= maksimumKarung

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onKembali) {
            Text("‹ Kembali ke hasil produksi", color = AccessColors.Green)
        }

        Text(
            text = "Siapkan label karung",
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
                Text("Batch produksi", color = Color(0xFFE3CB91))
                Text(
                    text = kodeBatch,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Lot bahan asal: $kodeLot", color = Color.White)
            }
        }

        if (pilihan == null) {
            AccessCard {
                Text(
                    text = "Belum ada hasil kemasan untuk batch ini.",
                    color = AccessColors.Muted
                )
            }
        } else {
            Text(
                text = "Produk dan kemasan",
                fontWeight = FontWeight.SemiBold,
                color = AccessColors.Ink
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { menuTerbuka = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${pilihan.namaProduk} · ${pilihan.kemasan}",
                        modifier = Modifier.weight(1f)
                    )
                    Text("▾")
                }

                DropdownMenu(
                    expanded = menuTerbuka,
                    onDismissRequest = { menuTerbuka = false }
                ) {
                    daftarKemasan.forEachIndexed { indeks, produk ->
                        DropdownMenuItem(
                            text = { Text("${produk.namaProduk} · ${produk.kemasan}") },
                            onClick = {
                                indeksTerpilih = indeks
                                menuTerbuka = false
                            }
                        )
                    }
                }
            }

            AccessCard {
                Text(
                    text = "Berat yang belum dilabeli",
                    color = AccessColors.Muted
                )
                Text(
                    text = "${formatAngka.format(pilihan.beratBelumDilabeliKg)} kg",
                    color = AccessColors.Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "Maksimal ${formatAngka.format(maksimumKarung)} karung penuh",
                    color = AccessColors.Green
                )
            }

            OutlinedTextField(
                value = jumlahTeks,
                onValueChange = { jumlahTeks = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Jumlah karung selesai dikemas") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = jumlahTeks.isNotBlank() && !jumlahValid,
                supportingText = {
                    if (jumlahTeks.isNotBlank() && !jumlahValid) {
                        Text("Isi jumlah karung penuh dalam batas yang tersedia.")
                    } else if (jumlahValid) {
                        Text("$jumlah karung akan mendapat $jumlah label unik.")
                    }
                }
            )

            Button(
                onClick = {
                    if (jumlah != null && jumlahValid) {
                        onLihatLabel(pilihan, jumlah)
                    }
                },
                enabled = jumlahValid,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccessColors.Green,
                    contentColor = Color.White
                )
            ) {
                Text("Lihat pratinjau label")
            }
        }
    }
}

@Preview(
    name = "Produksi · Siapkan label karung",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun SiapkanLabelKarungPreview() {
    SIRABASTheme {
        SiapkanLabelKarungScreen(
            kodeBatch = "PR/001",
            kodeLot = "BM/024",
            daftarKemasan = listOf(
                KemasanUntukLabelUi(
                    namaProduk = "Premium",
                    kemasan = "5 kg",
                    beratPerKarungKg = BigDecimal("5"),
                    beratBelumDilabeliKg = BigDecimal("180")
                )
            ),
            jumlahAwal = "36",
            onKembali = {},
            onLihatLabel = { _, _ -> }
        )
    }
}