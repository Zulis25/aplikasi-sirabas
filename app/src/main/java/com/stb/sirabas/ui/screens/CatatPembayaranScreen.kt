package com.stb.sirabas.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.math.BigInteger
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CatatPembayaranScreen(
    invoice: InvoicePenjualanUi,
    onKembali: () -> Unit,
    onSimpan: (nominal: Long, metode: String) -> Unit,
    nominalAwal: String = "",
    onSimpanDenganBukti: ((Long, String, Uri?) -> Unit)? = null
) {
    val formatAngka = remember {
        NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"))
    }

    var nominalTeks by rememberSaveable(invoice.nomor) {
        mutableStateOf(nominalAwal)
    }

    var metode by rememberSaveable(invoice.nomor) {
        mutableStateOf("Cash")
    }

    var buktiTransfer by rememberSaveable(invoice.nomor) {
        mutableStateOf<Uri?>(null)
    }

    val sisaSebelum = invoice.sisaPiutang
    val nominal = nominalTeks.toLongOrNull()

    val pesanKesalahan = when {
        nominalTeks.isEmpty() -> null
        nominal == null -> "Nominal terlalu besar."
        nominal <= 0L -> "Masukkan nominal lebih dari Rp0."
        nominal.toBigInteger() > sisaSebelum ->
            "Nominal melebihi sisa piutang."
        else -> null
    }

    val bisaSimpan = nominal != null &&
            nominal > 0L &&
            pesanKesalahan == null &&
            sisaSebelum.signum() > 0

    val sisaSetelah = if (bisaSimpan && nominal != null) {
        sisaSebelum - nominal.toBigInteger()
    } else {
        null
    }

    fun rupiah(nilai: BigInteger): String {
        return "Rp${formatAngka.format(nilai)}"
    }

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
            TextButton(
                onClick = onKembali,
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                Text("‹  Kembali ke invoice", color = AccessColors.Green)
            }

            Text(
                text = "Catat pembayaran",
                color = AccessColors.Ink,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF123F2E),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = invoice.pelanggan,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = invoice.nomor,
                        color = Color(0xFFE3CB91)
                    )

                    Spacer(Modifier.height(4.dp))

                    Text("Sisa piutang saat ini", fontSize = 14.sp)

                    Text(
                        text = rupiah(sisaSebelum),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                contentColor = AccessColors.Ink
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BarisPembayaran("Total invoice", rupiah(invoice.total))
                    BarisPembayaran(
                        "Sudah dibayar",
                        rupiah(invoice.jumlahDibayar.toBigInteger())
                    )

                    HorizontalDivider(color = AccessColors.Border)

                    OutlinedTextField(
                        value = nominalTeks,
                        onValueChange = { baru ->
                            if (baru.all { it in '0'..'9' }) {
                                nominalTeks = baru
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nominal pembayaran") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        isError = pesanKesalahan != null
                    )

                    if (pesanKesalahan != null) {
                        Text(
                            text = pesanKesalahan,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "Metode pembayaran",
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Cash", "Transfer Bank").forEach { pilihan ->
                            FilterChip(
                                selected = metode == pilihan,
                                onClick = {
                                    metode = pilihan

                                    if (pilihan == "Cash") {
                                        buktiTransfer = null
                                    }
                                },
                                label = { Text(pilihan) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFDDEFE4),
                                    selectedLabelColor = Color(0xFF125139)
                                )
                            )
                        }
                    }

                    // Satu pemilih gambar untuk pembayaran transfer ini.
                    if (metode == "Transfer Bank") {
                        BagianBuktiTransfer(
                            uri = buktiTransfer,
                            onGambarBerubah = { uri ->
                                buktiTransfer = uri
                            }
                        )
                    }
                }
            }

            if (sisaSetelah != null) {
                val lunas = sisaSetelah.signum() == 0

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (lunas) Color(0xFFE2F2E8) else Color(0xFFFFEFCB),
                    contentColor = if (lunas) Color(0xFF125139) else Color(0xFF704A00)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Setelah pembayaran",
                            fontWeight = FontWeight.Bold
                        )

                        BarisPembayaran("Sisa piutang", rupiah(sisaSetelah))
                        BarisPembayaran(
                            "Status invoice",
                            if (lunas) "Lunas" else "Piutang"
                        )
                    }
                }
            }
        }

        Surface(color = Color.White, shadowElevation = 3.dp) {
            Button(
                onClick = {
                    if (bisaSimpan && nominal != null) {
                        val lampiran = buktiTransfer.takeIf {
                            metode == "Transfer Bank"
                        }

                        // Jalankan satu callback agar cicilan dicatat satu kali.
                        if (onSimpanDenganBukti != null) {
                            onSimpanDenganBukti(nominal, metode, lampiran)
                        } else {
                            onSimpan(nominal, metode)
                        }
                    }
                },
                enabled = bisaSimpan,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccessColors.Green,
                    contentColor = Color.White
                )
            ) {
                Text("Simpan pembayaran")
            }
        }
    }
}

@Composable
private fun BarisPembayaran(judul: String, nilai: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(judul, modifier = Modifier.weight(1f))
        Text(nilai, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ContohCatatPembayaran(nominal: String) {
    val produk = ProdukPenjualanUi(
        kode = "PREMIUM-25-HIJAU",
        nama = "Beras Premium",
        kemasan = "25 kg hijau",
        satuanJual = "karung",
        stokTersedia = 12,
        hargaKatalog = 350_000L
    )

    val invoice = InvoicePenjualanUi(
        nomor = "INV/2026/002",
        tanggal = "11 November 2026",
        pelanggan = "MBG B",
        daftarItem = listOf(ItemPesananUi(produk, 2, 350_000L)),
        jumlahDibayar = 200_000L,
        metodePembayaran = "Cash"
    )

    SIRABASTheme {
        CatatPembayaranScreen(
            invoice = invoice,
            onKembali = {},
            onSimpan = { _, _ -> },
            nominalAwal = nominal
        )
    }
}

@Preview(
    name = "Pembayaran sebagian",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PembayaranSebagianPreview() {
    ContohCatatPembayaran(nominal = "150000")
}

@Preview(
    name = "Pelunasan invoice",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun PelunasanInvoicePreview() {
    ContohCatatPembayaran(nominal = "500000")
}