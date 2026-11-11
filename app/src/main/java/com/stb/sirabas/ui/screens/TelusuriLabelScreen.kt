package com.stb.sirabas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

data class PengirimanLabelUi(
    val nomorPesanan: String,
    val pelanggan: String,
    val waktuPindai: String,
    val waktuKeluar: String
)

data class JejakKarungUi(
    val kodeKarung: String,
    val namaProduk: String,
    val kemasan: String,
    val nomorKarung: Int,
    val jumlahKarung: Int,
    val kodeLot: String,
    val jenisBahan: String,
    val supplier: String,
    val asalDaerah: String,
    val tanggalMasuk: String,
    val kodeBatch: String,
    val tanggalProduksi: String,
    val pengiriman: PengirimanLabelUi? = null
)

@Composable
fun TelusuriLabelScreen(karung: JejakKarungUi) {
    val pengiriman = karung.pengiriman

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Telusuri label",
            color = AccessColors.Ink,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        // Identitas karung yang sedang ditelusuri.
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF123F2E),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Kode karung",
                    color = Color(0xFFE3CB91),
                    fontSize = 13.sp
                )
                Text(
                    text = karung.kodeKarung,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("${karung.namaProduk} · ${karung.kemasan}")

                Spacer(Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Text(
                        text = if (pengiriman == null) {
                            "Di gudang"
                        } else {
                            "Sudah dikirim"
                        },
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        ),
                        color = AccessColors.Green,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        KartuJejakKarung(
            judul = "Asal bahan baku",
            kode = karung.kodeLot,
            rincian = listOf(
                "Tanggal masuk" to karung.tanggalMasuk,
                "Jenis bahan" to karung.jenisBahan,
                "Supplier" to karung.supplier,
                "Asal daerah" to karung.asalDaerah
            )
        )

        KartuJejakKarung(
            judul = "Produksi dan pengemasan",
            kode = karung.kodeBatch,
            rincian = listOf(
                "Tanggal produksi" to karung.tanggalProduksi,
                "Produk" to karung.namaProduk,
                "Kemasan" to karung.kemasan,
                "Urutan karung" to
                        "${karung.nomorKarung} dari ${karung.jumlahKarung}"
            )
        )

        if (pengiriman != null) {
            KartuJejakKarung(
                judul = "Pengiriman",
                kode = pengiriman.nomorPesanan,
                rincian = listOf(
                    "Pelanggan" to pengiriman.pelanggan,
                    "Dipindai saat muat" to pengiriman.waktuPindai,
                    "Keluar gudang" to pengiriman.waktuKeluar
                )
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "Pengiriman",
                        color = AccessColors.Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Karung masih tersimpan di gudang.",
                        color = AccessColors.Muted
                    )
                }
            }
        }
    }
}

@Composable
private fun KartuJejakKarung(
    judul: String,
    kode: String,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = judul,
                color = AccessColors.Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = kode,
                color = AccessColors.Green,
                fontWeight = FontWeight.SemiBold
            )
            HorizontalDivider(color = AccessColors.Border)

            rincian.forEach { (label, nilai) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        color = AccessColors.Muted,
                        fontSize = 14.sp
                    )
                    Text(
                        text = nilai,
                        modifier = Modifier.weight(1.2f),
                        color = AccessColors.Ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private val contohKarungAdmin = JejakKarungUi(
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

@Preview(
    name = "Admin · Label karung di gudang",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun LabelDiGudangPreview() {
    SIRABASTheme {
        TelusuriLabelScreen(karung = contohKarungAdmin)
    }
}

@Preview(
    name = "Admin · Label karung sudah dikirim",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun LabelSudahDikirimPreview() {
    SIRABASTheme {
        TelusuriLabelScreen(
            karung = contohKarungAdmin.copy(
                pengiriman = PengirimanLabelUi(
                    nomorPesanan = "PS-001",
                    pelanggan = "MBG A",
                    waktuPindai = "12 Nov 2026, 08.10",
                    waktuKeluar = "12 Nov 2026, 08.25"
                )
            )
        )
    }
}