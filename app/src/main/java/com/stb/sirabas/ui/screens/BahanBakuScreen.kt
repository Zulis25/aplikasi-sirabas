package com.stb.sirabas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.stb.sirabas.ui.theme.SIRABASTheme
import java.text.NumberFormat
import java.util.Locale

data class BahanBakuUi(
    val kode: String,
    // Format tahun-bulan-tanggal agar mudah diurutkan.
    val tanggal: String,
    val jenis: String,
    val supplier: String,
    val asalDaerah: String,
    val beratKg: Double,
    val kadarAir: Double?,
    val derajatSosoh: Double?,
    val broken: Double?
)

@Composable
fun BahanBakuScreen(
    data: List<BahanBakuUi>? = null,
    onTambah: (() -> Unit)? = null
) {
    var pencarian by rememberSaveable {
        mutableStateOf("")
    }

    var tanggalTerpilih by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var menuTanggalTerbuka by remember {
        mutableStateOf(false)
    }

    val daftarTanggal = data.orEmpty()
        .map { it.tanggal }
        .distinct()
        .sortedDescending()

    val kataKunci = pencarian.trim()

    val hasilFilter = data.orEmpty()
        .filter { bahan ->
            val cocokTanggal =
                tanggalTerpilih == null ||
                        bahan.tanggal == tanggalTerpilih

            val cocokPencarian =
                bahan.kode.contains(kataKunci, ignoreCase = true) ||
                        bahan.jenis.contains(kataKunci, ignoreCase = true) ||
                        bahan.supplier.contains(kataKunci, ignoreCase = true) ||
                        bahan.asalDaerah.contains(kataKunci, ignoreCase = true)

            cocokTanggal && cocokPencarian
        }
        .sortedByDescending { it.tanggal }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AccessColors.Background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Bahan Baku Masuk",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccessColors.Ink
                    )

                    Text(
                        text = "Daftar penerimaan beras dan gabah.",
                        fontSize = 14.sp,
                        color = AccessColors.Muted
                    )
                }
            }

            if (onTambah != null) {
                item {
                    Button(
                        onClick = onTambah,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccessColors.Green,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        )
                    ) {
                        Text("+ Tambah bahan masuk")
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = pencarian,
                    onValueChange = { pencarian = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Cari bahan baku")
                    },
                    placeholder = {
                        Text("Kode, jenis, pemasok, atau daerah")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccessColors.Green,
                        unfocusedBorderColor = AccessColors.Border,
                        focusedLabelColor = AccessColors.Green,
                        cursorColor = AccessColors.Green
                    )
                )
            }

            item {
                Box {
                    OutlinedButton(
                        onClick = {
                            menuTanggalTerbuka = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = tanggalTerpilih
                                ?: "Semua tanggal",
                            color = AccessColors.Green
                        )
                    }

                    DropdownMenu(
                        expanded = menuTanggalTerbuka,
                        onDismissRequest = {
                            menuTanggalTerbuka = false
                        },
                        modifier = Modifier.heightIn(max = 320.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("Semua tanggal")
                            },
                            onClick = {
                                tanggalTerpilih = null
                                menuTanggalTerbuka = false
                            }
                        )

                        daftarTanggal.forEach { tanggal ->
                            DropdownMenuItem(
                                text = {
                                    Text(tanggal)
                                },
                                onClick = {
                                    tanggalTerpilih = tanggal
                                    menuTanggalTerbuka = false
                                }
                            )
                        }
                    }
                }
            }

            if (data != null) {
                item {
                    AccessNotice(
                        title = "${hasilFilter.size} penerimaan",
                        message = "Total berat masuk: " +
                                "${angkaBahan(hasilFilter.sumOf { it.beratKg })} kg"
                    )
                }
            }

            when {
                data == null -> {
                    item {
                        AccessCard {
                            Text(
                                text = "Data penerimaan belum tersedia",
                                color = AccessColors.Ink
                            )
                        }
                    }
                }

                hasilFilter.isEmpty() -> {
                    item {
                        AccessCard {
                            Text(
                                text = "Tidak ada penerimaan yang sesuai",
                                fontWeight = FontWeight.SemiBold,
                                color = AccessColors.Ink
                            )

                            Text(
                                text = "Coba ubah pencarian atau tanggal.",
                                color = AccessColors.Muted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                else -> {
                    items(
                        items = hasilFilter,
                        key = { it.kode }
                    ) { bahan ->
                        BahanBakuCard(bahan = bahan)
                    }
                }
            }
        }
    }
}

@Composable
private fun BahanBakuCard(
    bahan: BahanBakuUi
) {
    var detailTerbuka by rememberSaveable(bahan.kode) {
        mutableStateOf(false)
    }

    AccessCard {
        Text(
            text = "${bahan.kode} · ${bahan.tanggal}",
            color = AccessColors.Muted,
            fontSize = 12.sp
        )

        Text(
            text = bahan.jenis,
            color = AccessColors.Ink,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = bahan.supplier,
            color = AccessColors.Ink,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = bahan.asalDaerah,
            color = AccessColors.Muted,
            fontSize = 13.sp
        )

        HorizontalDivider(
            color = AccessColors.Border
        )

        Text(
            text = "Berat masuk: ${angkaBahan(bahan.beratKg)} kg",
            color = AccessColors.Green,
            fontWeight = FontWeight.SemiBold
        )

        TextButton(
            onClick = {
                detailTerbuka = !detailTerbuka
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (detailTerbuka) {
                    "Tutup hasil pengujian"
                } else {
                    "Lihat hasil pengujian"
                },
                color = AccessColors.Green
            )
        }

        if (detailTerbuka) {
            Surface(
                color = AccessColors.SoftGreen,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Hasil pengujian",
                        fontWeight = FontWeight.SemiBold,
                        color = AccessColors.Ink
                    )

                    Text(
                        text = "Kadar air: ${persenBahan(bahan.kadarAir)}",
                        color = AccessColors.Ink
                    )

                    Text(
                        text = "Derajat sosoh: " +
                                persenBahan(bahan.derajatSosoh),
                        color = AccessColors.Ink
                    )

                    Text(
                        text = "Broken: ${persenBahan(bahan.broken)}",
                        color = AccessColors.Ink
                    )
                }
            }
        }
    }
}

private fun angkaBahan(nilai: Double): String {
    return NumberFormat.getNumberInstance(
        Locale("id", "ID")
    ).apply {
        maximumFractionDigits = 2
    }.format(nilai)
}

private fun persenBahan(nilai: Double?): String {
    return if (nilai == null) {
        "Belum dicatat"
    } else {
        "${angkaBahan(nilai)}%"
    }
}

// Hanya untuk mencoba tampilan melalui Preview.
// Bukan data yang tersimpan di server.
fun contohBahanBaku(): List<BahanBakuUi> {
    return listOf(
        BahanBakuUi(
            kode = "BM/024",
            tanggal = "2026-11-11",
            jenis = "Gabah",
            supplier = "UD Tani Makmur",
            asalDaerah = "Karawang",
            beratKg = 1000.0,
            kadarAir = 14.0,
            derajatSosoh = null,
            broken = null
        ),
        BahanBakuUi(
            kode = "BM/023",
            tanggal = "2026-11-10",
            jenis = "Beras",
            supplier = "UD Padi Jaya",
            asalDaerah = "Indramayu",
            beratKg = 750.0,
            kadarAir = 13.0,
            derajatSosoh = 95.0,
            broken = 15.0
        )
    )
}

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun BahanBakuScreenPreview() {
    SIRABASTheme {
        BahanBakuScreen(
            data = contohBahanBaku()
        )
    }
}