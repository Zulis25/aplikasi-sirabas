package com.stb.sirabas.ui.screens

import androidx.activity.compose.BackHandler
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

data class RingkasanAdminUi(
    val namaAdmin: String,
    val tanggal: String,
    val bahanMasuk: String,
    val hasilUtama: String,
    val hasilSamping: String,
    val stokBahanBaku: String,
    val totalPenjualan: String,
    val jumlahInvoice: Int,
    val sisaPiutang: String
)

@Composable
fun BerandaAdminScreen(
    ringkasan: RingkasanAdminUi,
    jumlahPengajuan: Int,
    onBukaPengajuan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "Beranda Admin",
                color = AccessColors.Ink,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selamat datang, ${ringkasan.namaAdmin}",
                color = AccessColors.Muted
            )
            Text(
                text = ringkasan.tanggal,
                color = AccessColors.Muted,
                fontSize = 13.sp
            )
        }

        // Kartu ini membuka daftar pengajuan akun.
        Surface(
            onClick = onBukaPengajuan,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF123F2E),
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "Pengajuan akun",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (jumlahPengajuan > 0) {
                            "$jumlahPengajuan menunggu aktivasi"
                        } else {
                            "Semua pengajuan sudah ditinjau"
                        },
                        color = Color(0xFFE3CB91)
                    )
                }
                Text("Lihat ›", fontWeight = FontWeight.SemiBold)
            }
        }

        KartuRingkasanAdmin(
            judul = "Produksi hari ini",
            rincian = listOf(
                "Bahan baku diterima" to ringkasan.bahanMasuk,
                "Hasil utama" to ringkasan.hasilUtama,
                "Hasil samping" to ringkasan.hasilSamping
            )
        )

        KartuRingkasanAdmin(
            judul = "Stok gudang saat ini",
            rincian = listOf(
                "Bahan baku tersedia" to ringkasan.stokBahanBaku
            )
        )

        KartuRingkasanAdmin(
            judul = "Penjualan hari ini",
            rincian = listOf(
                "Invoice terbit" to "${ringkasan.jumlahInvoice} invoice",
                "Total penjualan" to ringkasan.totalPenjualan,
                "Sisa piutang invoice hari ini" to ringkasan.sisaPiutang
            )
        )
    }
}

@Composable
private fun KartuRingkasanAdmin(
    judul: String,
    rincian: List<Pair<String, String>>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, AccessColors.Border
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = judul,
                color = AccessColors.Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            rincian.forEachIndexed { indeks, (label, nilai) ->
                if (indeks > 0) {
                    HorizontalDivider(color = AccessColors.Border)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        color = AccessColors.Muted,
                        fontSize = 14.sp
                    )
                    Text(
                        text = nilai,
                        color = AccessColors.Ink,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Admin · Beranda",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun BerandaAdminPreview() {
    var bukaPengajuan by rememberSaveable { mutableStateOf(false) }

    val pengajuan = remember {
        mutableStateListOf(
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
    }

    val ringkasan = RingkasanAdminUi(
        namaAdmin = "Admin STB",
        tanggal = "11 November 2026",
        bahanMasuk = "1.000 kg",
        hasilUtama = "180 kg",
        hasilSamping = "10 kg",
        stokBahanBaku = "1.500 kg",
        totalPenjualan = "Rp700.000",
        jumlahInvoice = 1,
        sisaPiutang = "Rp500.000"
    )

    BackHandler(enabled = bukaPengajuan) {
        bukaPengajuan = false
    }

    SIRABASTheme {
        if (bukaPengajuan) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AccessColors.Background)
            ) {
                TextButton(
                    onClick = { bukaPengajuan = false },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("‹  Beranda Admin", color = AccessColors.Green)
                }

                Box(modifier = Modifier.weight(1f)) {
                    AktivasiAkunScreen(
                        daftarPengajuan = pengajuan,
                        onAktifkan = { akun, _ -> pengajuan.remove(akun) }
                    )
                }
            }
        } else {
            BerandaAdminScreen(
                ringkasan = ringkasan,
                jumlahPengajuan = pengajuan.size,
                onBukaPengajuan = { bukaPengajuan = true }
            )
        }
    }
