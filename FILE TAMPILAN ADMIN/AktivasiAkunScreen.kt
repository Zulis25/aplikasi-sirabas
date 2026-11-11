package com.stb.sirabas.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class PengajuanAkunUi(
    val id: String,
    val nama: String,
    val username: String,
    val whatsapp: String,
    val tanggalPengajuan: String
)

@Composable
fun AktivasiAkunScreen(
    daftarPengajuan: List<PengajuanAkunUi>,
    onAktifkan: (pengajuan: PengajuanAkunUi, role: String) -> Unit
) {
    var idTerpilih by rememberSaveable { mutableStateOf<String?>(null) }

    val pengajuanTerpilih = daftarPengajuan.firstOrNull {
        it.id == idTerpilih
    }

    BackHandler(enabled = pengajuanTerpilih != null) {
        idTerpilih = null
    }

    if (pengajuanTerpilih != null) {
        DetailPengajuanAkun(
            pengajuan = pengajuanTerpilih,
            onKembali = { idTerpilih = null },
            onAktifkan = { role ->
                onAktifkan(pengajuanTerpilih, role)
                idTerpilih = null
            }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Aktivasi akun",
                color = AccessColors.Ink,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text(
                text = "${daftarPengajuan.size} pengajuan menunggu",
                color = AccessColors.Muted
            )
        }

        if (daftarPengajuan.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Text(
                        text = "Semua pengajuan sudah ditinjau.",
                        modifier = Modifier.padding(20.dp),
                        color = AccessColors.Ink
                    )
                }
            }
        }

        items(daftarPengajuan, key = { it.id }) { pengajuan ->
            Surface(
                onClick = { idTerpilih = pengajuan.id },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, AccessColors.Border
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = pengajuan.nama,
                            color = AccessColors.Ink,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${pengajuan.username}",
                            color = AccessColors.Muted
                        )
                        Text(
                            text = pengajuan.tanggalPengajuan,
                            color = AccessColors.Muted,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "Tinjau ›",
                        color = AccessColors.Green,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailPengajuanAkun(
    pengajuan: PengajuanAkunUi,
    onKembali: () -> Unit,
    onAktifkan: (String) -> Unit
) {
    // Admin memilih role secara eksplisit untuk setiap pengajuan.
    var role by rememberSaveable(pengajuan.id) {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(
            onClick = onKembali,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            Text("‹  Daftar pengajuan", color = AccessColors.Green)
        }

        Text(
            text = "Tinjau pengajuan",
            color = AccessColors.Ink,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DataPengajuan("Nama lengkap", pengajuan.nama)
                HorizontalDivider(color = AccessColors.Border)
                DataPengajuan("Username", pengajuan.username)
                HorizontalDivider(color = AccessColors.Border)
                DataPengajuan("Nomor WhatsApp", pengajuan.whatsapp)
                HorizontalDivider(color = AccessColors.Border)
                DataPengajuan("Tanggal pengajuan", pengajuan.tanggalPengajuan)
            }
        }

        Text(
            text = "Role pengguna",
            color = AccessColors.Ink,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        listOf("Petugas Produksi", "Petugas Penjualan").forEach { pilihan ->
            val dipilih = role == pilihan

            Surface(
                selected = dipilih,
                onClick = { role = pilihan },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (dipilih) Color(0xFFE2F2E8) else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (dipilih) AccessColors.Green else AccessColors.Border
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RadioButton(
                        selected = dipilih,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AccessColors.Green
                        )
                    )
                    Text(pilihan, color = AccessColors.Ink)
                }
            }
        }

        Button(
            onClick = { role?.let(onAktifkan) },
            enabled = role != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccessColors.Green,
                contentColor = Color.White
            )
        ) {
            Text("Aktifkan akun")
        }
    }
}

@Composable
private fun DataPengajuan(label: String, nilai: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = AccessColors.Muted, fontSize = 13.sp)
        Text(
            nilai,
            color = AccessColors.Ink,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private val contohPengajuanAdmin = listOf(
    PengajuanAkunUi(
        id = "PG-001",
        nama = "Rina Putri",
        username = "RinaPutri",
        whatsapp = "0812 0000 0001",
        tanggalPengajuan = "11 November 2026"
    ),
    PengajuanAkunUi(
        id = "PG-002",
        nama = "Dedi Saputra",
        username = "DediSaputra",
        whatsapp = "0812 0000 0002",
        tanggalPengajuan = "10 November 2026"
    )
)

@Preview(
    name = "Admin · Daftar pengajuan",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun DaftarAktivasiPreview() {
    val daftar = remember {
        mutableStateListOf<PengajuanAkunUi>().apply {
            addAll(contohPengajuanAdmin)
        }
    }

    SIRABASTheme {
        AktivasiAkunScreen(
            daftarPengajuan = daftar,
            onAktifkan = { pengajuan, _ ->
                daftar.remove(pengajuan)
            }
        )
    }
}

@Preview(
    name = "Admin · Tinjau pengajuan",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun DetailAktivasiPreview() {
    SIRABASTheme {
        DetailPengajuanAkun(
            pengajuan = contohPengajuanAdmin.first(),
            onKembali = {},
            onAktifkan = {}
        )
    }
}