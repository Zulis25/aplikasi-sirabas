package com.stb.sirabas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme

enum class StatusPindaiKarung {
    SESUAI,
    VARIAN_SALAH,
    SUDAH_DIPINDAI
}

@Composable
fun HasilPindaiKarungScreen(
    nomorPesanan: String,
    namaPelanggan: String,
    kodeKarung: String,
    varianPesanan: String,
    varianTerpindai: String,
    status: StatusPindaiKarung,
    jumlahTercatat: Int,
    jumlahPesanan: Int,
    onPindaiLagi: () -> Unit,
    onKembali: () -> Unit
) {
    val judul: String
    val pesan: String
    val warnaLatar: Color
    val warnaTeks: Color

    when (status) {
        StatusPindaiKarung.SESUAI -> {
            judul = "Karung sesuai"
            pesan = "Karung ini sudah tercatat dan dapat dinaikkan ke angkutan."
            warnaLatar = Color(0xFFE3F1E8)
            warnaTeks = Color(0xFF174E39)
        }

        StatusPindaiKarung.VARIAN_SALAH -> {
            judul = "Varian tidak sesuai"
            pesan = "Sisihkan karung ini. Ambil karung sesuai pesanan lalu pindai labelnya."
            warnaLatar = Color(0xFFFBE9E6)
            warnaTeks = Color(0xFF8B3027)
        }

        StatusPindaiKarung.SUDAH_DIPINDAI -> {
            judul = "Karung sudah dipindai"
            pesan = "Kode ini sudah tercatat pada pesanan ini. Lanjutkan dengan karung berikutnya."
            warnaLatar = Color(0xFFFFF1D6)
            warnaTeks = Color(0xFF70500D)
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
            text = "Hasil pemindaian",
            color = AccessColors.Ink,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = warnaLatar,
            contentColor = warnaTeks,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = judul,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = pesan, lineHeight = 20.sp)
            }
        }

        AccessCard {
            Text(
                text = "Pesanan $nomorPesanan · $namaPelanggan",
                color = AccessColors.Ink,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider(color = AccessColors.Border)

            DetailPindai("Kode karung", kodeKarung)
            DetailPindai("Varian yang dipesan", varianPesanan)
            DetailPindai("Varian pada label", varianTerpindai)
        }

        AccessCard {
            Text("Karung tercatat", color = AccessColors.Muted)
            Text(
                text = "$jumlahTercatat dari $jumlahPesanan karung",
                color = AccessColors.Ink,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (jumlahTercatat < jumlahPesanan) {
            Button(
                onClick = onPindaiLagi,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccessColors.Green,
                    contentColor = Color.White
                )
            ) {
                Text("Pindai karung berikutnya")
            }
        }

        OutlinedButton(
            onClick = onKembali,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Kembali ke persiapan pengiriman")
        }
    }
}

@Composable
private fun DetailPindai(judul: String, nilai: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = judul, color = AccessColors.Muted, fontSize = 13.sp)
        Text(
            text = nilai,
            color = AccessColors.Ink,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ContohHasilPindai(status: StatusPindaiKarung) {
    val salahVarian = status == StatusPindaiKarung.VARIAN_SALAH

    SIRABASTheme {
        HasilPindaiKarungScreen(
            nomorPesanan = "PS-001",
            namaPelanggan = "MBG A",
            kodeKarung = when (status) {
                StatusPindaiKarung.SESUAI -> "PR002-K004"
                StatusPindaiKarung.VARIAN_SALAH -> "PR003-K001"
                StatusPindaiKarung.SUDAH_DIPINDAI -> "PR002-K003"
            },
            varianPesanan = "Premium · 25 kg hijau",
            varianTerpindai = if (salahVarian) {
                "Premium · 25 kg biru"
            } else {
                "Premium · 25 kg hijau"
            },
            status = status,
            jumlahTercatat = if (status == StatusPindaiKarung.SESUAI) 4 else 3,
            jumlahPesanan = 50,
            onPindaiLagi = {},
            onKembali = {}
        )
    }
}

@Preview(name = "01 · Karung sesuai", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewKarungSesuai() {
    ContohHasilPindai(StatusPindaiKarung.SESUAI)
}

@Preview(name = "02 · Varian salah", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewVarianSalah() {
    ContohHasilPindai(StatusPindaiKarung.VARIAN_SALAH)
}

@Preview(name = "03 · Sudah dipindai", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewSudahDipindai() {
    ContohHasilPindai(StatusPindaiKarung.SUDAH_DIPINDAI)
}