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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PenerimaanTersimpanScreen(
    bahan: BahanBakuUi,
    onCetakLabel: (BahanBakuUi) -> Unit,
    onKembaliKeDaftar: () -> Unit
) {
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
        Text(
            text = "Penerimaan tersimpan",
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
                    text = "Kode lot bahan masuk",
                    color = Color(0xFFE3CB91),
                    fontSize = 14.sp
                )
                Text(
                    text = bahan.kode,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${bahan.jenis} · ${formatBerat.format(bahan.beratKg)} kg",
                    color = Color.White,
                    fontSize = 17.sp
                )
            }
        }

        AccessCard {
            Text(
                text = "Detail penerimaan",
                color = AccessColors.Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text("Supplier: ${bahan.supplier}")
            Text("Asal bahan: ${bahan.asalDaerah}")
            Text("Tanggal diterima: ${bahan.tanggal}")
        }

        Button(
            onClick = { onCetakLabel(bahan) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccessColors.Green,
                contentColor = Color.White
            )
        ) {
            Text("Cetak label lot")
        }

        OutlinedButton(
            onClick = onKembaliKeDaftar,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Kembali ke daftar bahan masuk", color = AccessColors.Green)
        }
    }
}

@Preview(
    name = "Bahan masuk · Penerimaan tersimpan",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PenerimaanTersimpanPreview() {
    SIRABASTheme {
        PenerimaanTersimpanScreen(
            bahan = contohBahanBaku().first(),
            onCetakLabel = {},
            onKembaliKeDaftar = {}
        )
    }
}