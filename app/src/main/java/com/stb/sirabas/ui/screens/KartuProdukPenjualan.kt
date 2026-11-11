package com.stb.sirabas.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stb.sirabas.R
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

data class ProdukPenjualanUi(
    val kode: String,
    val nama: String,
    val kemasan: String,
    val satuanJual: String,
    val stokTersedia: Int,
    val hargaKatalog: Long,
    @DrawableRes val fotoRes: Int? = null
)

@Composable
fun KartuProdukPenjualan(
    produk: ProdukPenjualanUi,
    onTambah: (
        produk: ProdukPenjualanUi,
        jumlah: Int,
        hargaSatuan: Long
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatAngka = remember {
        NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"))
    }

    var hargaTeks by rememberSaveable(produk.kode, produk.hargaKatalog) {
        mutableStateOf(produk.hargaKatalog.toString())
    }

    var jumlahPilihan by rememberSaveable(produk.kode) {
        mutableStateOf(1)
    }

    val stok = produk.stokTersedia.coerceAtLeast(0)
    val jumlah = if (stok == 0) 0 else jumlahPilihan.coerceIn(1, stok)
    val harga = hargaTeks.toLongOrNull()
    val bisaDitambah = harga != null && harga > 0 && jumlah > 0

    val subtotal = if (harga != null && harga > 0 && jumlah > 0) {
        BigDecimal.valueOf(harga) * BigDecimal.valueOf(jumlah.toLong())
    } else {
        null
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = AccessColors.Green,
            onPrimary = Color.White
        )
    ) {
        OutlinedCard(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = Color.White,
                contentColor = AccessColors.Ink
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccessColors.SoftGreen)
                            .padding(5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val foto = produk.fotoRes

                        if (foto != null) {
                            Image(
                                painter = painterResource(id = foto),
                                contentDescription = "${produk.nama}, ${produk.kemasan}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = "Foto produk",
                                color = AccessColors.Green,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = produk.nama,
                            color = AccessColors.Ink,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = produk.kemasan,
                            color = AccessColors.Muted
                        )

                        Text(
                            text = if (stok > 0) {
                                "Tersedia $stok ${produk.satuanJual}"
                            } else {
                                "Stok habis"
                            },
                            color = if (stok > 0) AccessColors.Green else AccessColors.Muted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                HorizontalDivider(color = AccessColors.Border)

                Text(
                    text = "Harga katalog: Rp${formatAngka.format(produk.hargaKatalog)}" +
                            " / ${produk.satuanJual}",
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = hargaTeks,
                    onValueChange = { teks ->
                        if (teks.all { it in '0'..'9' }) {
                            hargaTeks = teks
                        }
                    },
                    label = { Text("Harga pesanan per ${produk.satuanJual}") },
                    prefix = { Text("Rp") },
                    singleLine = true,
                    enabled = stok > 0,
                    isError = stok > 0 && (harga == null || harga <= 0),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccessColors.Green,
                        focusedLabelColor = AccessColors.Green,
                        cursorColor = AccessColors.Green,
                        unfocusedBorderColor = AccessColors.Border,
                        focusedTextColor = AccessColors.Ink,
                        unfocusedTextColor = AccessColors.Ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Jumlah", fontWeight = FontWeight.SemiBold)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { jumlahPilihan = jumlah - 1 },
                            enabled = jumlah > 1,
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.semantics {
                                contentDescription = "Kurangi jumlah pesanan"
                            }
                        ) {
                            Text("−")
                        }

                        Text("$jumlah ${produk.satuanJual}")

                        OutlinedButton(
                            onClick = { jumlahPilihan = jumlah + 1 },
                            enabled = jumlah < stok,
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.semantics {
                                contentDescription = "Tambah jumlah pesanan"
                            }
                        ) {
                            Text("+")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Subtotal",
                            color = AccessColors.Muted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = subtotal?.let { "Rp${formatAngka.format(it)}" } ?: "—",
                            color = AccessColors.Green,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (harga != null && harga > 0 && jumlah > 0) {
                                onTambah(produk, jumlah, harga)
                            }
                        },
                        enabled = bisaDitambah,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccessColors.Green,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Tambah")
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Penjualan · Premium 25 kg hijau",
    showBackground = true,
    widthDp = 390
)
@Composable
private fun KartuProdukPenjualanPreview() {
    SIRABASTheme {
        Box(
            modifier = Modifier
                .background(AccessColors.Background)
                .padding(16.dp)
        ) {
            KartuProdukPenjualan(
                produk = ProdukPenjualanUi(
                    kode = "PREMIUM-25-HIJAU",
                    nama = "Beras Premium",
                    kemasan = "25 kg hijau",
                    satuanJual = "karung",
                    stokTersedia = 12,
                    hargaKatalog = 350_000L,
                    fotoRes = com.stb.sirabas.R.drawable.premium_25kg_hijau
                ),
                onTambah = { _, _, _ -> }
            )
        }
    }
}