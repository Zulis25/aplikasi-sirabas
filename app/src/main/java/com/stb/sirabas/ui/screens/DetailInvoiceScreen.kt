package com.stb.sirabas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.math.BigInteger
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DetailInvoiceScreen(
    nomorInvoice: String,
    tanggalPesanan: String,
    namaPt: String,
    alamatPt: String,
    teleponPt: String,
    namaPelanggan: String,
    daftarItem: List<ItemPesananUi>,
    jumlahDibayar: Long,
    metodePembayaran: String?,
    onUnduhGambar: () -> Unit,
    onCetak: () -> Unit
) {
    val formatAngka = remember {
        NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"))
    }

    fun rupiah(nominal: BigInteger): String =
        "Rp${formatAngka.format(nominal)}"

    val total = daftarItem.fold(BigInteger.ZERO) { jumlah, item ->
        jumlah + item.hargaSatuan.toBigInteger() * item.jumlah.toBigInteger()
    }

    val dibayar = jumlahDibayar.toBigInteger()
    val sisaPiutang = (total - dibayar).max(BigInteger.ZERO)
    val lunas = sisaPiutang == BigInteger.ZERO

    val warnaStatus = if (lunas) Color(0xFF176344) else Color(0xFF815300)
    val latarStatus = if (lunas) Color(0xFFE2F2E8) else Color(0xFFFFEFCB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccessColors.Background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Detail invoice",
                color = AccessColors.Ink,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                contentColor = AccessColors.Ink,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = namaPt,
                            color = AccessColors.Green,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (alamatPt.isNotBlank()) {
                            Text(alamatPt, color = AccessColors.Muted)
                        }
                        if (teleponPt.isNotBlank()) {
                            Text("Telp. $teleponPt", color = AccessColors.Muted)
                        }
                    }

                    HorizontalDivider(color = AccessColors.Border)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Nomor invoice",
                                color = AccessColors.Muted,
                                fontSize = 13.sp
                            )
                            Text(nomorInvoice, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            color = latarStatus,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (lunas) "Lunas" else "Piutang",
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 6.dp
                                ),
                                color = warnaStatus,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    BarisDetailInvoice("Tanggal pesanan", tanggalPesanan)
                    BarisDetailInvoice("Pelanggan", namaPelanggan)

                    HorizontalDivider(color = AccessColors.Border)

                    Text(
                        "Rincian pesanan",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    daftarItem.forEach { item ->
                        val produk = item.produk
                        val subtotal = item.hargaSatuan.toBigInteger() *
                                item.jumlah.toBigInteger()

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "${produk.nama} · ${produk.kemasan}",
                                fontWeight = FontWeight.SemiBold
                            )
                            BarisDetailInvoice(
                                judul = "${item.jumlah} ${produk.satuanJual} × " +
                                        rupiah(item.hargaSatuan.toBigInteger()),
                                nilai = rupiah(subtotal)
                            )
                        }
                    }

                    HorizontalDivider(color = AccessColors.Border)

                    BarisDetailInvoice(
                        judul = "Total invoice",
                        nilai = rupiah(total),
                        tebal = true
                    )
                    BarisDetailInvoice("Sudah dibayar", rupiah(dibayar))

                    if (jumlahDibayar > 0 && !metodePembayaran.isNullOrBlank()) {
                        BarisDetailInvoice("Metode pembayaran", metodePembayaran)
                    }

                    if (!lunas) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = latarStatus,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                BarisDetailInvoice(
                                    judul = "Sisa piutang",
                                    nilai = rupiah(sisaPiutang),
                                    tebal = true
                                )
                            }
                        }
                    }
                }
            }
        }

        Surface(color = Color.White, shadowElevation = 3.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onUnduhGambar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccessColors.Green
                    )
                ) {
                    Text("Unduh gambar")
                }

                Button(
                    onClick = onCetak,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccessColors.Green,
                        contentColor = Color.White
                    )
                ) {
                    Text("Cetak invoice")
                }
            }
        }
    }
}

@Composable
private fun BarisDetailInvoice(
    judul: String,
    nilai: String,
    tebal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = judul,
            modifier = Modifier.weight(1f),
            color = if (tebal) AccessColors.Ink else AccessColors.Muted,
            fontWeight = if (tebal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = nilai,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            color = AccessColors.Ink,
            fontWeight = if (tebal) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// Data khusus Preview agar kedua status bisa dilihat tanpa login.
@Composable
private fun ContohDetailInvoice(jumlahDibayar: Long, metode: String?) {
    val beras = ProdukPenjualanUi(
        kode = "PREMIUM-25-HIJAU",
        nama = "Beras Premium",
        kemasan = "25 kg hijau",
        satuanJual = "karung",
        stokTersedia = 12,
        hargaKatalog = 350_000L
    )

    SIRABASTheme {
        DetailInvoiceScreen(
            nomorInvoice = "INV/2026/001",
            tanggalPesanan = "11 November 2026",
            namaPt = "PT Sri Tani Berkah",
            alamatPt = "",
            teleponPt = "",
            namaPelanggan = "MBG A",
            daftarItem = listOf(
                ItemPesananUi(
                    produk = beras,
                    jumlah = 2,
                    hargaSatuan = 350_000L
                )
            ),
            jumlahDibayar = jumlahDibayar,
            metodePembayaran = metode,
            onUnduhGambar = {},
            onCetak = {}
        )
    }
}

@Preview(
    name = "Invoice Lunas",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun InvoiceLunasPreview() {
    ContohDetailInvoice(
        jumlahDibayar = 700_000L,
        metode = "Transfer Bank"
    )
}

@Preview(
    name = "Invoice Piutang",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun InvoicePiutangPreview() {
    ContohDetailInvoice(
        jumlahDibayar = 200_000L,
        metode = "Cash"
    )
}