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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.math.BigDecimal

@Composable
fun FormProdukReturScreen(
    kodeRetur: String,
    tanggal: String,
    pilihanProduk: List<Pair<String, String>>,
    onKembali: () -> Unit,
    onSimpan: (ProdukReturUi) -> Unit,
    isianAwal: ProdukReturUi? = null
) {
    var pelanggan by rememberSaveable(kodeRetur) {
        mutableStateOf(isianAwal?.pelanggan.orEmpty())
    }
    var produk by rememberSaveable(kodeRetur) {
        mutableStateOf(isianAwal?.namaProduk.orEmpty())
    }
    var kemasan by rememberSaveable(kodeRetur) {
        mutableStateOf(isianAwal?.kemasan.orEmpty())
    }
    var jumlahTeks by rememberSaveable(kodeRetur) {
        mutableStateOf(isianAwal?.jumlahKarung?.toString().orEmpty())
    }
    var beratTeks by rememberSaveable(kodeRetur) {
        mutableStateOf(isianAwal?.beratDiterimaKg?.toPlainString().orEmpty())
    }
    var alasan by rememberSaveable(kodeRetur) {
        mutableStateOf(isianAwal?.alasan.orEmpty())
    }
    var menuTerbuka by remember { mutableStateOf(false) }

    val jumlah = jumlahTeks.toIntOrNull()
    val berat = beratTeks.trim().replace(',', '.').toBigDecimalOrNull()
    val jumlahValid = jumlah != null && jumlah > 0
    val beratValid = berat != null && berat.signum() > 0

    val bisaDisimpan = pelanggan.isNotBlank() &&
            (produk to kemasan) in pilihanProduk &&
            jumlahValid && beratValid && alasan.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onKembali) {
            Text("‹ Kembali ke produk retur", color = AccessColors.Green)
        }

        Text(
            text = "Catat retur",
            color = AccessColors.Ink,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "$kodeRetur · $tanggal",
            color = AccessColors.Muted,
            fontSize = 14.sp
        )

        OutlinedTextField(
            value = pelanggan,
            onValueChange = { pelanggan = it },
            label = { Text("Nama pelanggan") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Produk yang dikembalikan",
                fontWeight = FontWeight.SemiBold,
                color = AccessColors.Ink
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { menuTerbuka = true },
                    enabled = pilihanProduk.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (produk.isBlank()) {
                            "Pilih produk dan kemasan"
                        } else {
                            "$produk · $kemasan"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Text("▾")
                }

                DropdownMenu(
                    expanded = menuTerbuka,
                    onDismissRequest = { menuTerbuka = false }
                ) {
                    pilihanProduk.forEach { (nama, ukuran) ->
                        DropdownMenuItem(
                            text = { Text("$nama · $ukuran") },
                            onClick = {
                                produk = nama
                                kemasan = ukuran
                                menuTerbuka = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = jumlahTeks,
            onValueChange = { jumlahTeks = it },
            label = { Text("Jumlah karung retur") },
            singleLine = true,
            isError = jumlahTeks.isNotBlank() && !jumlahValid,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = beratTeks,
            onValueChange = { beratTeks = it },
            label = { Text("Berat yang diterima (kg)") },
            supportingText = { Text("Gunakan hasil timbangan saat barang kembali.") },
            singleLine = true,
            isError = beratTeks.isNotBlank() && !beratValid,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = alasan,
            onValueChange = { alasan = it },
            label = { Text("Alasan retur") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (bisaDisimpan && jumlah != null && berat != null) {
                    onSimpan(
                        ProdukReturUi(
                            kode = kodeRetur,
                            tanggal = tanggal,
                            pelanggan = pelanggan.trim(),
                            namaProduk = produk,
                            kemasan = kemasan,
                            jumlahKarung = jumlah,
                            beratDiterimaKg = berat,
                            beratDiprosesKg = BigDecimal.ZERO,
                            alasan = alasan.trim()
                        )
                    )
                }
            },
            enabled = bisaDisimpan,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccessColors.Green
            )
        ) {
            Text("Simpan retur")
        }
    }
}

@Preview(
    name = "Produksi · Form catat retur",
    showBackground = true,
    widthDp = 390,
    heightDp = 900
)
@Composable
private fun FormProdukReturPreview() {
    val contoh = ProdukReturUi(
        kode = "RT/002",
        tanggal = "11 November 2026",
        pelanggan = "MBG A",
        namaProduk = "Premium",
        kemasan = "25 kg hijau",
        jumlahKarung = 2,
        beratDiterimaKg = BigDecimal("48"),
        beratDiprosesKg = BigDecimal.ZERO,
        alasan = "Satu karung bocor saat pengiriman."
    )

    SIRABASTheme {
        FormProdukReturScreen(
            kodeRetur = contoh.kode,
            tanggal = contoh.tanggal,
            pilihanProduk = listOf(
                "Premium" to "5 kg",
                "Premium" to "10 kg",
                "Premium" to "25 kg hijau",
                "Premium" to "25 kg biru",
                "Medium" to "25 kg",
                "Beras" to "50 kg"
            ),
            onKembali = {},
            onSimpan = {},
            isianAwal = contoh
        )
    }
}