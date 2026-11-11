package com.stb.sirabas.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme

@Composable
fun CariLabelKarungScreen(
    daftarKarung: List<JejakKarungUi>
) {
    var input by rememberSaveable { mutableStateOf("") }
    var kodeTerpilih by rememberSaveable { mutableStateOf<String?>(null) }
    var kodeGagal by rememberSaveable { mutableStateOf<String?>(null) }

    val karungTerpilih = daftarKarung.firstOrNull {
        it.kodeKarung == kodeTerpilih
    }

    val cariKarung: () -> Unit = {
        val kode = input.trim()

        if (kode.isNotEmpty()) {
            val hasil = daftarKarung.firstOrNull {
                it.kodeKarung.equals(kode, ignoreCase = true)
            }

            if (hasil != null) {
                kodeGagal = null
                kodeTerpilih = hasil.kodeKarung
            } else {
                kodeGagal = kode
            }
        }
    }

    BackHandler(enabled = karungTerpilih != null) {
        kodeTerpilih = null
    }

    if (karungTerpilih != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AccessColors.Background)
        ) {
            TextButton(
                onClick = { kodeTerpilih = null },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("‹  Cari label", color = AccessColors.Green)
            }

            Box(modifier = Modifier.weight(1f)) {
                TelusuriLabelScreen(karung = karungTerpilih)
            }
        }
    } else {
        FormCariLabelKarung(
            kode = input,
            kodeGagal = kodeGagal,
            onKodeBerubah = { baru ->
                input = baru
                kodeGagal = null
            },
            onCari = cariKarung
        )
    }
}

@Composable
private fun FormCariLabelKarung(
    kode: String,
    kodeGagal: String?,
    onKodeBerubah: (String) -> Unit,
    onCari: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Cari label karung",
            color = AccessColors.Ink,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Masukkan kode yang tercetak pada label karung.",
            color = AccessColors.Muted
        )

        OutlinedTextField(
            value = kode,
            onValueChange = onKodeBerubah,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Kode karung") },
            placeholder = { Text("PR001-K001") },
            singleLine = true,
            isError = kodeGagal != null,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onCari() }
            )
        )

        if (kodeGagal != null) {
            Text(
                text = "Kode $kodeGagal belum ditemukan. Periksa kembali kode pada label.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Button(
            onClick = onCari,
            enabled = kode.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccessColors.Green
            )
        ) {
            Text("Telusuri")
        }
    }
}

@Preview(
    name = "Admin · Cari label karung",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun CariLabelKarungPreview() {
    val contohKarung = JejakKarungUi(
        kodeKarung = "PR001-K001",
        namaProduk = "Beras Premium",
        kemasan = "5 kg",
        nomorKarung = 1,
        jumlahKarung = 36,
        kodeLot = "BM/025",
        jenisBahan = "Beras",
        supplier = "UD Zulis",
        asalDaerah = "Maros",
        tanggalMasuk = "11 Nov 2026",
        kodeBatch = "PR/001",
        tanggalProduksi = "11 Nov 2026"
    )

    SIRABASTheme {
        CariLabelKarungScreen(daftarKarung = listOf(contohKarung))
    }
}

@Preview(
    name = "Admin · Kode karung tidak ditemukan",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun CariLabelTidakDitemukanPreview() {
    SIRABASTheme {
        FormCariLabelKarung(
            kode = "PR001-K099",
            kodeGagal = "PR001-K099",
            onKodeBerubah = {},
            onCari = {}
        )
    }
}