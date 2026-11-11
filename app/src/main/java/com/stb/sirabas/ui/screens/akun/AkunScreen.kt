package com.stb.sirabas.ui.screens.akun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme

data class ProfilAkunUi(
    val namaLengkap: String,
    val username: String,
    val nomorWhatsApp: String,
    val role: String,
    val namaPerusahaan: String,
    val alamatPerusahaan: String,
    val teleponPerusahaan: String
)

@Composable
fun AkunScreen(
    profil: ProfilAkunUi,
    bahasa: String,
    modeGelap: Boolean,
    onEditProfil: () -> Unit,
    onPilihBahasa: () -> Unit,
    onUbahModeGelap: (Boolean) -> Unit,
    onTentang: () -> Unit,
    onKeluar: () -> Unit
) {
    val warna = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(warna.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Akun",
            color = warna.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF123F2E),
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = profil.role,
                    color = Color(0xFFE3CB91),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = profil.namaLengkap,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("@${profil.username}")
            }
        }

        KartuAkun(judul = "Profil pengguna") {
            DetailAkun("Nomor WhatsApp", profil.nomorWhatsApp)

            OutlinedButton(
                onClick = onEditProfil,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit profil")
            }
        }

        KartuAkun(judul = "Identitas perusahaan") {
            DetailAkun("Nama perusahaan", profil.namaPerusahaan)
            DetailAkun("Alamat", profil.alamatPerusahaan)
            DetailAkun("Nomor telepon", profil.teleponPerusahaan)
        }

        KartuAkun(judul = "Pengaturan") {
            BarisPilihanAkun(
                judul = "Bahasa",
                nilai = bahasa,
                onClick = onPilihBahasa
            )

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mode gelap")
                Switch(
                    checked = modeGelap,
                    onCheckedChange = onUbahModeGelap
                )
            }

            HorizontalDivider()

            BarisPilihanAkun(
                judul = "Tentang aplikasi",
                nilai = "SIRABAS",
                onClick = onTentang
            )
        }

        OutlinedButton(
            onClick = onKeluar,
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Text("Keluar dari akun", color = warna.error)
        }
    }
}

@Composable
private fun KartuAkun(
    judul: String,
    isi: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = judul,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            isi()
        }
    }
}

@Composable
private fun DetailAkun(label: String, nilai: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Text(text = nilai, fontSize = 16.sp)
    }
}

@Composable
private fun BarisPilihanAkun(
    judul: String,
    nilai: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(judul, modifier = Modifier.weight(1f))
            Text(nilai, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("›")
        }
    }
}

@Preview(
    name = "Akun · Petugas Produksi",
    showBackground = true,
    widthDp = 390,
    heightDp = 960
)
@Composable
private fun AkunProduksiPreview() {
    SIRABASTheme {
        AkunScreen(
            profil = ProfilAkunUi(
                namaLengkap = "Dedi",
                username = "Dedi",
                nomorWhatsApp = "08xx xxxx 1024",
                role = "Petugas Produksi",
                namaPerusahaan = "PT. Sri Tani Berkah",
                alamatPerusahaan = "Belum diisi",
                teleponPerusahaan = "Belum diisi"
            ),
            bahasa = "Bahasa Indonesia",
            modeGelap = false,
            onEditProfil = {},
            onPilihBahasa = {},
            onUbahModeGelap = {},
            onTentang = {},
            onKeluar = {}
        )
    }
}